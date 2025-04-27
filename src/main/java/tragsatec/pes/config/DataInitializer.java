package tragsatec.pes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tragsatec.pes.persistence.entity.RoleEntity;
import tragsatec.pes.persistence.repository.RoleRepository;

// Se ha comentado la anotación @Component para evitar que se ejecute al iniciar la aplicación
//@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
