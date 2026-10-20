package assistant.main.javaServer.Dto;

import java.util.List;

public record CategoryNode(Long id,String name,List<CategoryNode> children) {
}
