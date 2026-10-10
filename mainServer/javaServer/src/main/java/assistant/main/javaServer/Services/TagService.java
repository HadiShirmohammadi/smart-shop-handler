package assistant.main.javaServer.Services;

import assistant.main.javaServer.Entities.Tag;
import assistant.main.javaServer.Repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;
    public Tag getOrCreateTag(String name) {
        return tagRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(name.trim());
                    return tagRepository.save(t);
                });
    }
}
