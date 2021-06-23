package dev.highright96.springbatch.part4;

import dev.highright96.springbatch.part5.Orders;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet {

    private final UserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<User> users = createUsers();
        Collections.shuffle(users);
        userRepository.saveAll(users);
        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            users.add(User.builder()
                .orders(Collections.singletonList(Orders.builder()
                    .amount(1_000)
                    .createdDate(LocalDate.of(2020, 11, 1))
                    .itemName("item" + i)
                    .build()))
                .username("test username" + i)
                .build());
        }

        for (int i = 100; i < 200; i++) {
            users.add(User.builder()
                .orders(Collections.singletonList(Orders.builder()
                    .amount(200_000)
                    .createdDate(LocalDate.of(2020, 11, 2))
                    .itemName("item" + i)
                    .build()))
                .username("test username" + i)
                .build());
        }

        for (int i = 200; i < 300; i++) {
            users.add(User.builder()
                .orders(Collections.singletonList(Orders.builder()
                    .amount(300_000)
                    .createdDate(LocalDate.of(2020, 11, 3))
                    .itemName("item" + i)
                    .build()))
                .username("test username" + i)
                .build());
        }

        for (int i = 300; i < 400; i++) {
            users.add(User.builder()
                .orders(Collections.singletonList(Orders.builder()
                    .amount(500_000)
                    .createdDate(LocalDate.of(2020, 11, 4))
                    .itemName("item" + i)
                    .build()))
                .username("test username" + i)
                .build());
        }

        return users;
    }


}
