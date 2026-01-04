package ch.hearc.ig.guideresto.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestLog {
    private static final Logger logger = LogManager.getLogger(TestLog.class);
    public static void main(String[] args) {
        logger.info("Ça marche !");
        logger.error("Erreur test !");
    }
}

