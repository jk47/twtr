/**
 * Created by johnkotz on 4/9/16.
 */
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.DesiredCapabilities

waiting {
    timeout = 2
}

driver = {
    def path = System.properties['os.name'].toLowerCase().contains('windows') ?
            'node_modules\\.bin\\chromedriver.exe' : './node_modules/.bin/chromedriver'
    System.setProperty("webdriver.chrome.driver", path);
    def driverInstance = System.properties['browser']?.toString()?.toLowerCase() == 'chrome' ? new ChromeDriver(new DesiredCapabilities()) : new FirefoxDriver()
    driverInstance.manage().window().maximize()
    driverInstance
}
