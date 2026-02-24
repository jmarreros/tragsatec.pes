package com.chc.pes.service.reporte;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTEscenarioProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.dto.general.EstacionPesUtProjection;
import com.chc.pes.dto.general.UnidadTerritorialProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;
import com.chc.pes.persistence.repository.estructura.PesUtEstacionRepository;
import com.chc.pes.persistence.repository.general.UnidadTerritorialRepository;
import com.chc.pes.service.general.DemarcacionService;
import com.chc.pes.util.DateUtils;
import com.chc.pes.util.DocumentWordUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ReporteWordUtSequiaService {
    @Value("${file.report-dir}")
    private String reportDir;

    @Value("${file.temporal-dir}")
    private String temporalDir;

    private final DemarcacionService demarcacionService;
    private final ReporteUtSequiaService reporteUtSequiaService;
    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;
    private final UnidadTerritorialRepository unidadTerritorialRepository;
    private final PesUtEstacionRepository pesUtEstacionRepository;

    public ReporteWordUtSequiaService(DemarcacionService demarcacionService, ReporteUtSequiaService reporteUtSequiaService, IndicadorUtSequiaRepository indicadorUtSequiaRepository, UnidadTerritorialRepository unidadTerritorialRepository, PesUtEstacionRepository pesUtEstacionRepository) {
        this.demarcacionService = demarcacionService;
        this.reporteUtSequiaService = reporteUtSequiaService;
        this.indicadorUtSequiaRepository = indicadorUtSequiaRepository;
        this.unidadTerritorialRepository = unidadTerritorialRepository;
        this.pesUtEstacionRepository = pesUtEstacionRepository;
    }

    public String downloadReportePDF(Integer anioPropuesto, Integer mes, String tipo) {
        DocumentWordUtils.crearDirectorioSiNoExiste(temporalDir);

        // Primero generamos el reporte en Word, luego lo convertimos a PDF --
        // Archivo Word fue solicitado inicialmente
        generarReporteWord(anioPropuesto, mes, tipo);
        return temporalDir + "/Reporte_UTS_" + tipo + ".docx";

    }

    public void generarReporteWord(Integer anioPropuesto, Integer mes, String tipo) {
        String archivoOrigen = reportDir + "/UTS_" + tipo + ".docx";
        String archivoFinal = temporalDir + "/Reporte_UTS_" + tipo + ".docx";

        // Establecer el año real hidrológico si es necesario
        Integer anioHidrologico = anioPropuesto;
        if (mes < 10) {
            anioHidrologico = anioPropuesto - 1;
        }

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(archivoOrigen))) {

            // Cambiar mes y año en el documento
            DocumentWordUtils.cambiarMesYAnioEnParrafo(document, mes, anioPropuesto);

            // Obtener información de la demarcacion
            DemarcacionProjection demarcacionInfo = getDemarcacionInfo(tipo.toLowerCase());
            Integer demarcacionId = demarcacionInfo.getId();
            String demarcacionCodigo = demarcacionInfo.getCodigo();

            // Obtenemos los datos de escenario para las UT de la demarcación
            // para mes/año específicos
            List<IndicadorUTEscenarioProjection> listUTEscenario = indicadorUtSequiaRepository.findByAnioMesDemarcacionUTEscenario(anioPropuesto, mes, demarcacionId);

            // Procesar y reemplazar los SVG en el documento
            String imgUTSs = DocumentWordUtils.procesarSVGFile('S', reportDir, temporalDir, listUTEscenario, demarcacionCodigo);

            // Insertar la imagen principal en el documento
            DocumentWordUtils.insertaImagenPrincipal(document, imgUTSs);
            DocumentWordUtils.insertarLeyendaImagen(document, "ESCENARIOS DE SEQUÍA");

            // Crear un párrafo vacío después de la imagen
            document.createParagraph();

            // Crear tabla de datos UT principal en el documento
            List<IndicadorDemarcacionFechaDataProjection> datos = datosDemarcacionUTSequia(anioHidrologico, mes, tipo);
            DocumentWordUtils.crearTablaUT(document, datos, anioHidrologico);


            if (datos != null && !datos.isEmpty())
                DocumentWordUtils.insertarLeyendaImagen(document, "INDICADORES DE SEQUÍA POR UTE ");

            List<UnidadTerritorialProjection> uTsPorDemarcacionSequia = getUTsPorDemarcacionSequia(demarcacionId);
            int uts = uTsPorDemarcacionSequia.size();

            // Recorremos todas las Unidades Territoriales de la demarcación
            for (int i = 0; i < uts; i++) {

                UnidadTerritorialProjection utList = uTsPorDemarcacionSequia.get(i);

                // Siguiente contenido
                DocumentWordUtils.encabezadoH2(document, utList.getCodigo() + " - " + utList.getNombre());

                List<EstacionPesUtProjection> estacionesPesUt = pesUtEstacionRepository.findEstacionesPesIdWithCoeficienteByTipoAndUT('S', utList.getId());

                // Buscar la imagen correspondiente a la UTE y escenario actual
                // del mes
                String pathImgUtActual = DocumentWordUtils.nombreImagenUTActual(reportDir, 'S', listUTEscenario, utList);
                String tituloPeriodoActual = DateUtils.obtenerNombreMesCapitalizado(mes) + " - " + anioPropuesto;

                // Crear la sección de contenido 1x2, tabla de estaciones UT e
                // imagen de escenario
                DocumentWordUtils.crearContenidoSimple1x2(document, estacionesPesUt, pathImgUtActual, tituloPeriodoActual, i);

                // Obtener detalles de las estaciones por UT y año
                List<IndicadorUTFechaDataProjection> datosUTFecha = obtenerDatosUTFecha(utList.getId(), anioHidrologico, mes);
                List<IndicadorUTFechaDataProjection> totalesUTFecha = obtenerTotalesUTFecha(utList.getId(), anioHidrologico, mes);

                String escenario = DocumentWordUtils.getCurrentUTEscenario(utList.getId(), listUTEscenario);
                Double valorIndicador = DocumentWordUtils.getCurrentUTIndicadorTotalMes(utList.getId(), mes, totalesUTFecha);
                DocumentWordUtils.insertarLeyendaTabla(document, 'S', anioPropuesto, mes, valorIndicador, escenario);
                DocumentWordUtils.crearTablaDatosEstacionesUT(document, datosUTFecha, totalesUTFecha, utList.getNombre());

                // Comentario UT inferior
                if (valorIndicador != null)
                    DocumentWordUtils.insertarComentarioUt(document, utList.getComentario());

                // Gráfico
                List<Map<String, Object>> datosGrafico = DocumentWordUtils.prepararDatosTotalesParaGrafico(totalesUTFecha);


                // Insertar el gráfico en la nueva página
                String rutaGrafico = DocumentWordUtils.generarGraficoLineas('S', temporalDir, utList.getCodigo(), anioHidrologico, datosGrafico);
                DocumentWordUtils.insertarGraficoUT(document, rutaGrafico);

                // Finaliza documento
                DocumentWordUtils.finalizarDocumento(document);
            }

            try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                document.write(out);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<IndicadorUTFechaDataProjection> filtrarIndicadoresUTEFecha(List<IndicadorUTFechaDataProjection> indicadores, int anio, int mes) {
        int anioFinal = mes < 10 ? anio + 1 : anio;
        return indicadores.stream().filter(i -> i.getAnio() < anioFinal || (i.getAnio() == anioFinal && i.getMes() <= mes)).toList();
    }

    private List<IndicadorDemarcacionFechaDataProjection> filtrarIndicadoresDemarcacionFecha(List<IndicadorDemarcacionFechaDataProjection> indicadores, int anio, int mes) {
        int anioFinal = mes < 10 ? anio + 1 : anio;
        return indicadores.stream().filter(i -> i.getAnio() < anioFinal || (i.getAnio() == anioFinal && i.getMes() <= mes)).toList();
    }

    private List<IndicadorUTFechaDataProjection> obtenerDatosUTFecha(Integer utId, Integer anio, Integer mes) {
        return filtrarIndicadoresUTEFecha(reporteUtSequiaService.getUTEstacionFecha(utId, anio), anio, mes);
    }

    private List<IndicadorUTFechaDataProjection> obtenerTotalesUTFecha(Integer utId, Integer anio, Integer mes) {
        return filtrarIndicadoresUTEFecha(reporteUtSequiaService.getTotalDataUTFecha(utId, anio), anio, mes);
    }

    private List<UnidadTerritorialProjection> getUTsPorDemarcacionSequia(Integer demarcacionId) {
        return unidadTerritorialRepository.findUnidadesTerritorialesByTipoDemarcacionAndPes('S', demarcacionId);
    }

    private DemarcacionProjection getDemarcacionInfo(String ubicacion) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('S');
        return demarcacionesEscasez.stream().filter(d -> d.getNombre().toLowerCase().contains(ubicacion)).findFirst().orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + ubicacion));
    }

    private List<IndicadorDemarcacionFechaDataProjection> datosDemarcacionUTSequia(Integer anio, Integer mes, String tipo) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('S');
        // Buscar en la lista de demarcaciones en el campo de nombre que tenga
        // el texto: "oriental" u "occidental" según el tipo
        DemarcacionProjection demarcacionTipo = demarcacionesEscasez.stream().filter(d -> d.getNombre().toLowerCase().contains(tipo)).findFirst().orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + tipo));
        Integer demarcacionId = demarcacionTipo.getId();

        return filtrarIndicadoresDemarcacionFecha(reporteUtSequiaService.getAllDataFechaDemarcacion(demarcacionId, anio, "prep3"), anio, mes);
    }
}
