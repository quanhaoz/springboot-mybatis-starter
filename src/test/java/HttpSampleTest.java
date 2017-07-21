import com.zhizus.mybatis.Application;
import com.zhizus.mybatis.mapper.SampleMapper;
import com.zhizus.mybatis.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by Dempe on 2017/7/1 0001.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class HttpSampleTest {

    @Autowired
    SampleMapper sampleMapper;

    @Test
    public void sampleTest(){
        User user = sampleMapper.selectById(1);
        System.out.println(user);
    }
}
