/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package it.user;

import com.google.common.base.Throwables;
import com.sonar.orchestrator.Orchestrator;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import util.user.UserRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test SSO authentication (using HTTP headers).
 * <p>
 * It starts its own server as it's using a different authentication system
 */
public class SsoAuthenticationTest {

  private static final String LOGIN_HEADER = "H-Login";
  private static final String NAME_HEADER = "H-Name";
  private static final String EMAIL_HEADER = "H-Email";
  private static final String GROUPS_HEADER = "H-Groups";

  static final String USER_LOGIN = "tester";
  static final String USER_NAME = "Tester";
  static final String USER_EMAIL = "tester@email.com";

  static final String GROUP_1 = "group-1";
  static final String GROUP_2 = "group-2";
  static final String GROUP_3 = "group-3";

  @ClassRule
  public static final Orchestrator orchestrator = Orchestrator.builderEnv()
    .setServerProperty("sonar.sso.enable", "true")
    .setServerProperty("sonar.sso.loginHeader", LOGIN_HEADER)
    .setServerProperty("sonar.sso.nameHeader", NAME_HEADER)
    .setServerProperty("sonar.sso.emailHeader", EMAIL_HEADER)
    .setServerProperty("sonar.sso.groupsHeader", GROUPS_HEADER)
    .build();

  @ClassRule
  public static UserRule USER_RULE = UserRule.from(orchestrator);

  @Before
  public void resetData() throws Exception {
    USER_RULE.resetUsers();
  }

  @Test
  public void authenticate() {
    call(USER_LOGIN, USER_NAME, USER_EMAIL, null);

    USER_RULE.verifyUserExists(USER_LOGIN, USER_NAME, USER_EMAIL);
  }

  @Test
  public void authenticate_with_only_login() throws Exception {
    call(USER_LOGIN, null, null, null);

    USER_RULE.verifyUserExists(USER_LOGIN, USER_LOGIN, null);
  }

  @Test
  public void update_user_when_headers_are_updated() {
    call(USER_LOGIN, USER_NAME, USER_EMAIL, null);
    USER_RULE.verifyUserExists(USER_LOGIN, USER_NAME, USER_EMAIL);

    // As we don't keep the JWT token is the test, the user is updated
    call(USER_LOGIN, "new name", "new email", null);
    USER_RULE.verifyUserExists(USER_LOGIN, "new name", "new email");
  }

  @Test
  public void authenticate_with_groups() {
    call(USER_LOGIN, null, null, GROUP_1);

    USER_RULE.verifyUserGroupMembership(USER_LOGIN, GROUP_1);
  }

  @Test
  public void synchronize_groups_when_authenticating_existing_user() throws Exception {
    USER_RULE.createGroup(GROUP_1);
    USER_RULE.createGroup(GROUP_2);
    USER_RULE.createGroup(GROUP_3);
    USER_RULE.createUser(USER_LOGIN, "password");
    USER_RULE.associateGroupsToUser(USER_LOGIN, GROUP_1, GROUP_2);

    call(USER_LOGIN, null, null, GROUP_2 + "," + GROUP_3);

    USER_RULE.verifyUserGroupMembership(USER_LOGIN, GROUP_2, GROUP_3);
  }

  @Test
  public void authentication_with_local_user_is_possible_when_no_header() throws Exception {
    USER_RULE.createUser(USER_LOGIN, "password");

    checkLocalAuthentication(USER_LOGIN, "password");
  }

  @Test
  public void display_message_in_ui_but_not_in_log_when_unauthorized_exception() throws Exception {
    Response response = doCall("invalid login $", null, null, null);

    assertThat(response.code()).isEqualTo(200);
    assertThat(response.body().string()).contains("You're not authorized to access this page. Please contact the administrator");

    List<String> logsLines = FileUtils.readLines(orchestrator.getServer().getWebLogs(), Charsets.UTF_8);
    assertThat(logsLines).doesNotContain("org.sonar.server.exceptions.BadRequestException: user.bad_login");
    USER_RULE.verifyUserDoesNotExist(USER_LOGIN);
  }

  private static Response call(String login, @Nullable String name, @Nullable String email, @Nullable String groups) {
    return doCall(login, name, email, groups);
  }

  private static Response doCall(String login, @Nullable String name, @Nullable String email, @Nullable String groups) {
    Request.Builder requestBuilder = new Request.Builder().get().url(orchestrator.getServer().getUrl())
      .addHeader(LOGIN_HEADER, login);
    if (name != null) {
      requestBuilder.addHeader(NAME_HEADER, name);
    }
    if (email != null) {
      requestBuilder.addHeader(EMAIL_HEADER, email);
    }
    if (groups != null) {
      requestBuilder.addHeader(GROUPS_HEADER, groups);
    }
    try {
      return new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
        .newCall(requestBuilder.build()).execute();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private boolean checkLocalAuthentication(String login, String password) {
    String result = orchestrator.getServer().wsClient(login, password).get("/api/authentication/validate");
    return result.contains("{\"valid\":true}");
  }

}
