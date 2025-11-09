package com.chc.pes.service.reporte;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTEscenarioProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.dto.general.EstacionPesUtProjection;
import com.chc.pes.dto.general.UnidadTerritorialProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;
import com.chc.pes.persistence.repository.estructura.PesUtEstacionRepository;
import com.chc.pes.persistence.repository.general.UnidadTerritorialRepository;
import com.chc.pes.service.general.DemarcacionService;

import com.chc.pes.util.DateUtils;
import com.chc.pes.util.DocumentWordUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;

import java.util.List;
import java.util.Map;

@Service
public class ReporteWordUtEscasezService {

    @Value("${file.report-dir}")
    private String reportDir;

    @Value("${file.temporal-dir}")
    private String temporalDir;

    private final DemarcacionService demarcacionService;
    private final ReporteUtEscasezService reporteUtEscasezService;
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;
    private final UnidadTerritorialRepository unidadTerritorialRepository;
    private final PesUtEstacionRepository pesUtEstacionRepository;

    public ReporteWordUtEscasezService(DemarcacionService demarcacionService, ReporteUtEscasezService reporteUtEscasezService, IndicadorUtEscasezRepository indicadorUtEscasezRepository, UnidadTerritorialRepository unidadTerritorialRepository, PesUtEstacionRepository pesUtEstacionRepository) {
        this.demarcacionService = demarcacionService;
        this.reporteUtEscasezService = reporteUtEscasezService;
        this.indicadorUtEscasezRepository = indicadorUtEscasezRepository;
        this.unidadTerritorialRepository = unidadTerritorialRepository;
        this.pesUtEstacionRepository = pesUtEstacionRepository;
    }

    public String downloadReporteWord(Integer anioPropuesto, Integer mes, String tipo) {
        generarReporteWord(anioPropuesto, mes, tipo);
        return temporalDir + "/Reporte_UTE_" + tipo + ".docx";
    }


    public void generarReporteWord(Integer anioPropuesto, Integer mes, String tipo) {
        String archivoOrigen = reportDir + "/UTE_" + tipo + ".docx";
        String archivoFinal = temporalDir + "/Reporte_UTE_" + tipo + ".docx";

        // Establecer el año real hidrológico si es necesario
        Integer anioHidrologico = anioPropuesto;
        if ( mes < 10 ) {
            anioHidrologico = anioPropuesto - 1;
        }


        try (XWPFDocument document = new XWPFDocument(new FileInputStream(archivoOrigen))) {

            // Cambiar mes y año en el documento
            DocumentWordUtils.cambiarMesYAnioEnParrafo(document, mes, anioPropuesto);

            // Obtener información de la demarcacion
            DemarcacionProjection demarcacionInfo = getDemarcacionInfo(tipo.toLowerCase());
            Integer demarcacionId = demarcacionInfo.getId();
            String demarcacionCodigo = demarcacionInfo.getCodigo();

            // Obtenemos los datos de escenario para las UT de la demarcación para mes/año específicos
            List<IndicadorUTEscenarioProjection> listUTEscenario = indicadorUtEscasezRepository.findByAnioMesDemarcacionUTEscenario(anioPropuesto, mes, demarcacionId);

            // Procesar y reemplazar los SVG en el documento
            String imgUTEs = DocumentWordUtils.procesarSVGFile('E', reportDir, temporalDir, listUTEscenario, demarcacionCodigo);

            // Insertar la imagen principal en el documento
            DocumentWordUtils.insertaImagenPrincipal(document, imgUTEs);
            DocumentWordUtils.insertarLeyendaImagen(document, "ESCENARIOS DE ESCASEZ");

            // Crear un párrafo vacío después de la imagen
            document.createParagraph();

            // Crear tabla de datos UT principal en el documento
            List<IndicadorDemarcacionFechaDataProjection> datos = datosDemarcacionUTEscasez(anioHidrologico, tipo);
            DocumentWordUtils.crearTablaUT(document, datos);

            DocumentWordUtils.insertarLeyendaImagen(document, "INDICADORES DE ESCASEZ POR UTE ");

            // Configurar orientación horizontal DESPUÉS de la tabla
            XWPFParagraph paraHorizontal = document.createParagraph();
            DocumentWordUtils.configurarOrientacionHorizontal(paraHorizontal);

            DocumentWordUtils.configurarMargenes(paraHorizontal, 720, 720, 720, 720);
            DocumentWordUtils.agregarSaltoDePagina(paraHorizontal);

            List<UnidadTerritorialProjection> utsPorDemarcacionEscasez = getUTsPorDemarcacionEscasez(demarcacionId);
            int uts = utsPorDemarcacionEscasez.size();

            // Recorremos todas las Unidades Territoriales de la demarcación
            for (int i = 0; i < uts ; i++) {

                UnidadTerritorialProjection utList = utsPorDemarcacionEscasez.get(i);

                // Siguiente contenido
                DocumentWordUtils.encabezadoH2(document, utList.getCodigo() + " - " + utList.getNombre());

                List<EstacionPesUtProjection> estacionesPesUt = pesUtEstacionRepository.findEstacionesPesIdWithCoeficienteByTipoAndUT('E', utList.getId());

                // Buscar la imagen correspondiente a la UTE y escenario actual del mes
                String pathImgUtActual = DocumentWordUtils.nombreImagenUTActual(reportDir, 'E', listUTEscenario, utList);
                String tituloPeriodoActual = DateUtils.obtenerNombreMesCapitalizado(mes) + " - " + anioPropuesto;

                // Crear la sección de contenido 1x2, tabla de estaciones UT e imagen de escenario
                DocumentWordUtils.crearContenido1x2(document, estacionesPesUt, pathImgUtActual, tituloPeriodoActual);

                // Obtener detalles de las estaciones por UT y año
                List<IndicadorUTFechaDataProjection> datosUTFecha = obtenerDatosUTFecha(utList.getId(), anioHidrologico);
                List<IndicadorUTFechaDataProjection> totalesUTFecha = obtenerTotalesUTFecha(utList.getId(), anioHidrologico);

                String escenario = DocumentWordUtils.getCurrentUTEscenario(utList.getId(), listUTEscenario);
                Double valorIndicador = DocumentWordUtils.getCurrentUTIndicadorTotal(utList.getId(), totalesUTFecha);
                DocumentWordUtils.insertarLeyendaTabla(document, 'E', anioPropuesto, mes ,  valorIndicador, escenario);
                DocumentWordUtils.crearTablaDatosEstacionesUT(document, datosUTFecha, totalesUTFecha, utList.getNombre());

                // Comentario UT inferior
                DocumentWordUtils.insertarComentarioUt(document, utList.getComentario());

                // Gráfico
                List<Map<String, Object>> datosGrafico = DocumentWordUtils.prepararDatosTotalesParaGrafico(totalesUTFecha);
                DocumentWordUtils.generarGraficoLineas('E', temporalDir, utList.getCodigo(), datosGrafico);


                // Solo agregar salto de página si no es la última UT
                if (i < uts - 1) {
                    // Crear una nueva página
                    DocumentWordUtils.agregarSaltoDePagina(document.createParagraph());
                    // Insertar un nuevo párrafo para dar un espacio
                    document.createParagraph();
                }

                // Insertar el gráfico en la nueva página
                String rutaGrafico = temporalDir + "/grafico_UTE_" + utList.getCodigo() + ".png";
                DocumentWordUtils.insertarGraficoUT(document, rutaGrafico);

                // Establecer márgenes
                XWPFParagraph paraMargenesReducidos = document.createParagraph();
                DocumentWordUtils.configurarMargenes(paraMargenesReducidos, 1800, 720, 720, 720);
            }

            try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                document.write(out);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private List<IndicadorUTFechaDataProjection> obtenerDatosUTFecha(Integer utId, Integer anio) {
        return reporteUtEscasezService.getUTEstacionFecha(utId, anio);
    }

    private List<IndicadorUTFechaDataProjection> obtenerTotalesUTFecha(Integer utId, Integer anio) {
        return reporteUtEscasezService.getTotalDataUTFecha(utId, anio);
    }

    private List<UnidadTerritorialProjection> getUTsPorDemarcacionEscasez(Integer demarcacionId) {
        return unidadTerritorialRepository.findUnidadesTerritorialesByTipoDemarcacionAndPes('E', demarcacionId);
    }

    private DemarcacionProjection getDemarcacionInfo(String ubicacion) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('E');
        return demarcacionesEscasez.stream()
                .filter(d -> d.getNombre().toLowerCase().contains(ubicacion))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + ubicacion));
    }

    private List<IndicadorDemarcacionFechaDataProjection> datosDemarcacionUTEscasez(Integer anio, String tipo) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('E');
        // Buscar en la lista de demarcaciones en el campo de nombre que tenga el texto: "oriental" u "occidental" según el tipo
        DemarcacionProjection demarcacionTipo = demarcacionesEscasez.stream()
                .filter(d -> d.getNombre().toLowerCase().contains(tipo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + tipo));
        Integer demarcacionId = demarcacionTipo.getId();

        return reporteUtEscasezService.getAllDataFechaDemarcacion(demarcacionId, anio);
    }

}
