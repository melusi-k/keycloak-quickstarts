package org.keycloak.quickstart;

import com.google.gson.JsonObject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.quickstart.page.IndexPage;
import org.keycloak.quickstart.page.LoginPage;
import org.keycloak.quickstart.page.ProfilePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.keycloak.quickstart.utils.WaitUtils.waitForPageToLoad;
import static org.keycloak.quickstart.utils.WaitUtils.waitTextToBePresent;

/**
 * Created by abstractj on 3/13/17.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ArquillianTest {

    private static final String WEBAPP_SRC = "src/main/webapp";

    @Page
    private IndexPage indexPage;

    @Page
    private LoginPage loginPage;

    @Page
    private ProfilePage profilePage;

    @Deployment(name = "service-jee-jaxrs", order = 1, testable = false)
    public static Archive<?> createTestArchive1() throws IOException {
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                new File("../service-jee-jaxrs/target/service.war"));
    }

    @Deployment(name = "app-profile-html5", order = 2, testable = false)
    public static Archive<?> createTestArchive2() throws IOException {
        return ShrinkWrap.create(WebArchive.class, "app-profile-html5.war")
                .addAsWebResource(new File(WEBAPP_SRC, "app.js"))
                .addAsWebResource(new File(WEBAPP_SRC, "index.html"))
                .addAsWebResource(new File(WEBAPP_SRC, "keycloak.js"))
                .addAsWebResource(new File(WEBAPP_SRC, "styles.css"))
                .addAsWebResource(new File("config", "keycloak.json"));

    }

    @Drone
    private WebDriver webDriver;

    @ArquillianResource
    @OperateOnDeployment("app-profile-html5")
    private URL contextRoot;

    @Before
    public void setup() {
        webDriver.navigate().to(contextRoot);
    }

    @Test
    public void testLogin() throws InterruptedException {
        try {
            indexPage.clickLogin();
            waitForPageToLoad(webDriver);
            loginPage.login("admin", "admin");
            waitForPageToLoad(webDriver);
            assertTrue(waitTextToBePresent(webDriver, By.id("username"), "admin"));
            profilePage.clickLogout();
        } catch (Exception e) {
            fail("Should display logged in user");
        }
    }

    @Test
    public void testProfileMenu() {
        try {
            indexPage.clickLogin();
            waitForPageToLoad(webDriver);
            loginPage.login("admin", "admin");
            waitForPageToLoad(webDriver);
            profilePage.clickToken();
            JsonObject json = profilePage.getTokenContent();
            assertNotNull("JSON content should not be empty", json);
            assertEquals(json.get("aud").getAsString(), "app-profile-html5");
            assertFalse(json.get("session_state").isJsonNull());
            webDriver.navigate().to(contextRoot);
            profilePage.clickLogout();
        } catch (Exception e) {
            fail("Should display logged in user");
        }
    }

    @Test
    public void testAccessAccountManagement() {
        try {
            indexPage.clickLogin();
            waitForPageToLoad(webDriver);
            loginPage.login("admin", "admin");
            waitForPageToLoad(webDriver);
            profilePage.clickAccount();
            assertEquals("Keycloak Account Management", webDriver.getTitle());
            webDriver.navigate().to(contextRoot);
            profilePage.clickLogout();
        } catch (Exception e) {
            fail("Should display account management page");
        }
    }


}