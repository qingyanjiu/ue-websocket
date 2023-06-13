package com.transcode.iot;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ComponentScan("com.transcode")
@ActiveProfiles("test")
public class BaseTest {
}
