package org.acme;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@QuarkusMain
public class Main {
    public static void main(String... args) {
        Quarkus.run(MyApp.class, args);
    }

    public static class MyApp implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            log.info("Inside QuarkusApplication run ...");
            Quarkus.waitForExit();
            return 0;
        }
    }

}
