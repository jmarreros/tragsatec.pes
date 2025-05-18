package tragsatec.pes.controller.general;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import tragsatec.pes.dto.UnidadTerritorialRequestDTO;
    import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
    import tragsatec.pes.service.general.UnidadTerritorialService;

    import java.util.List;

    @RestController
    @RequestMapping("unidades-territoriales")
    public class UnidadTerritorialController {
        private final UnidadTerritorialService unidadTerritorialService;

        @Autowired
        public UnidadTerritorialController(UnidadTerritorialService unidadTerritorialService) {
            this.unidadTerritorialService = unidadTerritorialService;
        }

        @GetMapping
        public List<UnidadTerritorialEntity> getAll() {
            return unidadTerritorialService.findAll();
        }

        @GetMapping("/{id}")
        public ResponseEntity<UnidadTerritorialEntity> getById(@PathVariable Integer id) {
            return unidadTerritorialService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping
        public UnidadTerritorialEntity create(@RequestBody UnidadTerritorialRequestDTO request) {
            return unidadTerritorialService.createUnidadTerritorial(request);
        }


        @PutMapping("/{id}")
        public ResponseEntity<UnidadTerritorialEntity> update(@PathVariable Integer id, @RequestBody UnidadTerritorialRequestDTO requestDTO) {
            UnidadTerritorialEntity updatedEntity = unidadTerritorialService.updateUnidadTerritorial(id, requestDTO);
            return ResponseEntity.ok(updatedEntity);
        }
    }