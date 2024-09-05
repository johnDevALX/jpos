package net.ekene.jposclient;

import lombok.RequiredArgsConstructor;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.q2.Q2;
import org.jpos.q2.iso.QMUX;
import org.jpos.util.NameRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SpringBootApplication
public class JposClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(JposClientApplication.class, args);
    }

//    @Bean
//    public Q2 q2(){
//        Q2 q2 = new Q2();
//        q2.start();
//        return q2;
//    }
//
//    @Bean
//    public MUX mux(Q2 q2) throws NameRegistrar.NotFoundException {
//        while (!q2.ready()){
//            ISOUtil.sleep(10);
//        }
//        return QMUX.getMUX("my-mux");
//    }

    @Override
    public void run(String... args) throws Exception {
        Q2 q2 = new Q2();
        Thread thread = new Thread(q2);
        thread.start();
    }

}
