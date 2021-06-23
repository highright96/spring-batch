package dev.highright96.springbatch.part4;

import dev.highright96.springbatch.part5.OrderStatistic;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UserConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final UserRepository userRepository;

    private final EntityManagerFactory emf;

    private final DataSource dataSource;

    @Bean
    public Job userJob() throws Exception {
        return jobBuilderFactory.get("userJob")
            .incrementer(new RunIdIncrementer())
            .start(this.saveUserStep())
            .next(this.userLevelUpStep())
            .next(this.orderStatisticsStep(null))
            .listener(new LevelUpJopExecutionListener(userRepository))
            .build();
    }

    @Bean
    @JobScope
    public Step orderStatisticsStep(@Value("#{jobParameters[date]}") String date) throws Exception {
        return this.stepBuilderFactory.get("orderStatisticStep")
            .<OrderStatistic, OrderStatistic>chunk(100)
            .reader(orderStatisticsItemReader(date))
            .writer(orderStatisticsItemWriter(date))
            .build();
    }

    private ItemReader<? extends OrderStatistic> orderStatisticsItemReader(String date)
        throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", yearMonth.atDay(1));
        parameters.put("endDate", yearMonth.atEndOfMonth());

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("created_date", Order.ASCENDING);

        JdbcPagingItemReader<OrderStatistic> itemReader = new JdbcPagingItemReaderBuilder<OrderStatistic>()
            .name("orderStatisticsItemReader")
            .dataSource(dataSource)
            .rowMapper((resultSet, i) -> OrderStatistic.builder()
                .amount(resultSet.getString(1))
                .date(LocalDate.parse(resultSet.getString(2), DateTimeFormatter.ISO_LOCAL_DATE))
                .build())
            .selectClause("sum(amount), created_date")
            .fromClause("orders")
            .whereClause("created_date >= :startDate and created_date <= :endDate")
            .groupClause("created_date")
            .parameterValues(parameters)
            .sortKeys(sortKey)
            .pageSize(100)
            .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    private ItemWriter<? super OrderStatistic> orderStatisticsItemWriter(String date)
        throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);
        String fileName = yearMonth.getYear() + "년_" + yearMonth.getMonthValue() + "월_일별_주문_금액.csv";

        BeanWrapperFieldExtractor<OrderStatistic> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"amount", "date"});

        DelimitedLineAggregator<OrderStatistic> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(" ");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<OrderStatistic> itemWriter = new FlatFileItemWriterBuilder<OrderStatistic>()
            .resource(new FileSystemResource("output/" + fileName))
            .lineAggregator(lineAggregator)
            .name("orderStatisticsItemWriter")
            .encoding("UTF-8")
            .headerCallback(writer -> writer.write("total_amount, date"))
            .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    public Step saveUserStep() {
        return stepBuilderFactory.get("saveUserStep")
            .tasklet(new SaveUserTasklet(userRepository))
            .build();
    }

    @Bean
    public Step userLevelUpStep() throws Exception {
        return this.stepBuilderFactory.get("userLevelUpStep")
            .<User, User>chunk(100)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
    }

    private ItemWriter<? super User> itemWriter() {
        return users -> users.forEach(user -> {
            user.levelUp();
            userRepository.save((user));
        });
    }

    private ItemProcessor<? super User, ? extends User> itemProcessor() {
        return user -> {
            if (user.availableLevelUp()) {
                return user;
            }
            return null;
        };
    }

    private ItemReader<? extends User> itemReader() throws Exception {
        JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
            .queryString("select u from User u")
            .entityManagerFactory(emf)
            .pageSize(100)
            .name("userItemReader")
            .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }
}
