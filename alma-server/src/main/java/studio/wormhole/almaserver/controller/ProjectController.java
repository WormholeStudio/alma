package studio.wormhole.almaserver.controller;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.wormhole.almaserver.enums.State;
import studio.wormhole.almaserver.enums.Token;
import studio.wormhole.almaserver.model.Project;

@RestController
@RequestMapping("project")
public class ProjectController {

  @PostMapping("/list")
  public List<Project> list() {
    Project project = Project.builder()
        .id(1)
        .context("stc")
        .icon("https://icon.com")
        .state(State.NORMAL)
        .tokenId(Token.STC.getId())
        .name("测试项目")
        .createAt(new Date())
        .context("我就是拿来测试的,别理我")
        .context("abc").build();
    Project project1 = Project.builder()
        .id(2)
        .context("ALM")
        .icon("https://icon.com")
        .state(State.NORMAL)
        .tokenId(Token.ALM.getId())
        .name("ALM测试项目")
        .createAt(new Date())
        .context("我就是拿来测试的,别理我")
        .context("ALM").build();
    return ImmutableList.of(project,project1);
  }
}
