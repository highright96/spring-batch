package dev.highright96.springbatch.part3;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person> {

    private final RetryTemplate retryTemplate;

    public PersonValidationRetryProcessor() {
        this.retryTemplate = new RetryTemplateBuilder()
            .maxAttempts(3)
            .retryOn(NotFoundNameException.class)
            .build();
    }

    @Override
    public Person process(Person item) throws Exception {
        return this.retryTemplate.execute(context -> {
            /*
            [RetryCallBack]
            NotFoundNameException이 발생하면 RetryCallBack이 maxAttempts 만큼 반복한다.
            maxAttempts 를 넘어가면 RecoveryCallBack 이 발생한다.
            */
            if (item.isNotEmptyName()) {
                return item;
            }
            throw new NotFoundNameException();
        }, context -> {
            //RecoveryCallBack
            return item.unknownName();
        });
    }
}
