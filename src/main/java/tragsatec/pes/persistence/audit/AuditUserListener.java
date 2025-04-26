package tragsatec.pes.persistence.audit;

import com.fasterxml.jackson.core.JsonToken;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import tragsatec.pes.persistence.entity.UserEntity;

public class AuditUserListener {

    @PostPersist
    @PostUpdate
    public void onPostPersist(UserEntity entity) {
        System.out.println("UserEntity created/updated: " + entity.toString());
    }

    @PreRemove
    public void onPreDelete(UserEntity entity) {
        System.out.println("UserEntity deleted: " + entity.toString());
    }
}
