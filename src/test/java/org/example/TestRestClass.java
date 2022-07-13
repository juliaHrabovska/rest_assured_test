package org.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.example.entity.User;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TestRestClass {
    private RequestSpecification requestSpecification;

    @BeforeMethod
    public void authSetUp() {
        requestSpecification =
                RestAssured.given().auth().oauth2("8d3edc50fd5dbb75c78aa0e6b003827314f21f4aa8f03facd79465c96ce44c55");
        setCommonParams(requestSpecification);
    }

    @AfterMethod
    public void cleanUp() {
        requestSpecification = null;
    }

    @Test
    public void getAllUsersTest() {
        Response response = requestSpecification.expect().statusCode(HttpStatus.SC_OK).log().ifError()
                .when().get("https://gorest.co.in/public/v2/users");
        List<User> users = response.jsonPath().getList("", User.class);
        Assert.assertEquals(users.size(), 10, "Expected users list size doesn't equal to actual");
    }

    @Test
    public void getUsersByIdTest() {
        Response responseAllUsers = requestSpecification.expect().statusCode(HttpStatus.SC_OK).log().ifError()
                .when().get("https://gorest.co.in/public/v2/users");
        List<User> users = responseAllUsers.jsonPath().getList("", User.class);

        User expectedUser = users.get(0);
        Response responseUserById = requestSpecification.expect().statusCode(HttpStatus.SC_OK).log().ifError()
                .when().get("https://gorest.co.in/public/v2/users/" + expectedUser.getId());
        User actualUser = responseUserById.as(User.class);
        Assert.assertEquals(actualUser.getName(), expectedUser.getName(), "Expected user doesn't have correct name");
    }

    @Test
    public void createUsersTest() {
        User expectedUser = createUser();
        Response response = requestSpecification.body(expectedUser).expect().statusCode(HttpStatus.SC_CREATED).log().ifError()
                .when().post("https://gorest.co.in/public/v2/users/");
        User createdUser = response.as(User.class);
        Assert.assertEquals(createdUser.getName(), expectedUser.getName(), "Expected user doesn't have correct name");
    }

    @Test
    public void deleteUsersTest() {
        Response responseUserCreation =
                requestSpecification.body(createUser()).expect().statusCode(HttpStatus.SC_CREATED).log().ifError()
                .when().post("https://gorest.co.in/public/v2/users/");

        User userForDeletion = responseUserCreation.as(User.class);
        requestSpecification.expect().statusCode(HttpStatus.SC_NO_CONTENT).log().ifError()
                .when().delete("https://gorest.co.in/public/v2/users/" + userForDeletion.getId());
        Response response = requestSpecification.expect().statusCode(HttpStatus.SC_OK).log().ifError()
                .when().get("https://gorest.co.in/public/v2/users");
        List<User> users = response.jsonPath().getList("", User.class);
        Assert.assertFalse(users.contains(userForDeletion), "Expected users list doesn't contain deleted element");
    }

    private void setCommonParams(RequestSpecification requestSpecification) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        requestSpecification.headers(headers);
    }

    private User createUser() {
        Random random = new Random();
        User user = new User();
        user.setName("test" + random.nextInt());
        user.setEmail("testEmail" + random.nextInt() + "@gmail.com");
        user.setGender("Male");
        user.setStatus("active");

        return user;
    }
}
