package com.torry.data;

import com.mongodb.BasicDBObject;
import com.torry.data.service.UserService;
import com.torry.data.util.SpringUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
	@Autowired
	UserService userService;

	@Test
	public void testEvent() throws Exception {
		assertNotNull(userService);
		SpringUtil.doEvent("/demo_user/user_info/1",new BasicDBObject("user_no",1));
	}


}
