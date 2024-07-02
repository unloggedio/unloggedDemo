package org.unlogged.demo.jspdemo.wfm.Services;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.unlogged.demo.jspdemo.wfm.Dao.UsersRepository;
import org.unlogged.demo.jspdemo.wfm.Models.Entities.DeepClass;
import org.unlogged.demo.jspdemo.wfm.Models.Entities.User;
import org.unlogged.demo.jspdemo.wfm.Models.Entities.UserListInfo;

import java.util.Arrays;
import java.util.List;

import static org.unlogged.demo.OtelConfig.makeSpan;

@Service
@Component
public class UserService {
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("unlogged-spring-maven-demo");

    @Autowired
    UsersRepository usersRepository;

    public User getUser(String username) {
        User u = usersRepository.getUserByUsername(username);
        //some comment
        return u;
    }

    public User getUser(long userId) {
        Span span = tracer.spanBuilder("custom_tracer").startSpan();
        makeSpan(span, "input.userId", userId);

        User user = usersRepository.getUserByUserId(userId);
        makeSpan(span, "mockData.1", user);

        makeSpan(span, "output", user);
        span.end();
        return user;
    }

    public void addUser(User user) {
        usersRepository.save(user);
    }

    public String throwExceptionTest() {
        String a = "aaa ccc";
        String s = "a".split(" ")[1];
        return a;
    }

    public String many(String a, String b, String c, String d) {
        return "some1";
    }

    public List<User> getAllUsers() {
        return usersRepository.findAll();
    }

    public long getCountOfUsers() {
        Span span = tracer.spanBuilder("custom_tracer").startSpan();

        long val = usersRepository.count();
        makeSpan(span, "mockData.1", val);

        makeSpan(span, "output", val);
        span.end();
        return val;
    }

    public UserListInfo getULO() {
        return new UserListInfo(0, usersRepository.findAll());
    }

    public int getNumber() {
        return 14;
    }

    public DeepClass getDeepClassList() {
        List<User> usersList = Arrays.asList(new User(), new User(1, "a", "a", "a"));
        return new DeepClass(usersList);
    }
}

//-javaagent:"/Users/testerfresher/.videobug/videobug-java-agent.jar=i=com.jsp.jspwfm" --add-opens=java.base/java.util=ALL-UNNAMED

