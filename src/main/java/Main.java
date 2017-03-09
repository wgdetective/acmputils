import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Wayne on 08.03.2017.
 */
public class Main {

    public static final String TABLE_XPATH = "/html/body/table/tbody/tr[3]/td/table/tbody/tr/td[2]/table/tbody/tr[2]/td[2]/center/table";
    public static final String SOLVED_TASKS_LINK = "html/body/table/tbody/tr[3]/td/table/tbody/tr/td[2]/table/tbody/tr[2]/td[2]/table/tbody/tr/td[1]/p[1]";

    public static void main(String[] args) throws IOException {
        /*final List<String> names = new ArrayList<>();
        names.add("Волынец Владислав");*/
        final List<String> names = Files.readAllLines(new File("names.txt").toPath());
        int number = 2;
        try (final FileWriter fileWriter = new FileWriter("result.csv")) {
            final WebDriver driver = new HtmlUnitDriver();
            for (String name : names) {
                final String url = "http://acmp.ru/index.asp?main=rating&str=" + URLEncoder.encode(name, "WINDOWS-1251");
                //System.out.println(url);
                driver.get(url);
                fileWriter.append(number + "," + name + ",");
                final String result = checkName(driver);
                System.out.printf("%4s%30s%20s\n", number++, name, result);
                fileWriter.append(result).append("\n");
            }
            driver.close();
        }
    }

    private static String checkName(final WebDriver driver) throws IOException {
        final List<WebElement> candidates = driver.findElements(By.xpath(TABLE_XPATH + "/tbody/tr"));
        if (candidates.size() > 1) {
            boolean first = true;
            for (WebElement candidate : candidates) {
                if (first) {
                    first = false;
                } else {
                    final WebElement numberOfTasks = candidate.findElement(By.xpath("td[4]"));
                    final WebElement rate = candidate.findElement(By.xpath("td[5]"));
                    if (Integer.valueOf(numberOfTasks.getText()) >= 3
                            && Integer.valueOf(rate.getText()) >= 70) {
                        final WebElement nameElement = candidate.findElement(By.xpath("td[2]/a"));
                        nameElement.click();
                        try {
                            final WebElement element = driver.findElement(By.xpath(SOLVED_TASKS_LINK));
                            final String tasks = element.getText();
                            final List<String> tasksList = Arrays.asList(tasks.split(" "));
                            if (tasksList.contains("5")
                                    && tasksList.contains("6")
                                    && tasksList.contains("9")){
                                return "+";
                            }
                        } catch (final NoSuchElementException e) {
                            e.printStackTrace();
                        }
                        driver.navigate().back();
                    }
                }
            }
            return "Задачи не решены";
        } else {
            return "Не нашел профиль";
        }
    }
}
