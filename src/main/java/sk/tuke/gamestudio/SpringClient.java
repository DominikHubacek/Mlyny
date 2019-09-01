package sk.tuke.gamestudio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import sk.tuke.gamestudio.game.hubacek.ninemen.consoleUI.Console;
import sk.tuke.gamestudio.service.*;

@Configuration
@SpringBootApplication
@ComponentScan(basePackages = {"sk.tuke.gamestudio"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "sk.tuke.gamestudio.server.*"))
public class SpringClient {
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(SpringClient.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public CommandLineRunner runner(Console ui) {
        return args -> ui.start();
    }

    @Bean
    public Console consoleUI() {
        return new Console();
    }

    @Bean
    public ScoreService scoreService() {
        //return new ScoreServiceJDBC();
        //return new ScoreServiceJPA();
        return new ScoreServiceRestClient();
    }

    @Bean
    public CommentService commentService() {
        //return new CommentServiceJDBC();
        //return new CommentServiceJPA();
        return new CommentServiceRestClient();
    }

    @Bean
    public RatingService ratingService() {
        //return new RatingServiceJDBC();
        //return new RatingServiceJPA();
        return new RatingServiceRestClient();
    }

}
