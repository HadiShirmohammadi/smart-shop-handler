package assistant.main.javaServer.Dto;

import java.util.List;

public record CategoryNode(String name, Long parentId, List<CategoryNode> children) {
}
