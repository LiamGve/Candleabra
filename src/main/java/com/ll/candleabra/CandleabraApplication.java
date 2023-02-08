package com.ll.candleabra;

import com.ll.candleabra.configuration.BootstrapConfiguration;
import com.ll.candleabra.configuration.MapperConfiguration;
import com.ll.candleabra.configuration.WebSocketConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@Import({
        MapperConfiguration.class,
        WebSocketConfiguration.class,
        BootstrapConfiguration.class
})
@SpringBootConfiguration
@EnableAutoConfiguration
public class CandleabraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandleabraApplication.class, args);
    }
}
