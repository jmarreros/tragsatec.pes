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
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ReporteWordUtEscasezService {

    @Value("${file.report-dir}")
    private String reportDir;
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

    public void generarReporteWord(Integer anio, Integer mes, String tipo) throws IOException {
        String archivoOrigen = reportDir + "/UTE_" + tipo + ".docx";
        String archivoFinal = reportDir + "/Reporte_UTE_" + tipo + ".docx";


        try (XWPFDocument document = new XWPFDocument(new FileInputStream(archivoOrigen))) {

            // Cambiar mes y año en el documento
            DocumentWordUtils.cambiarMesYAnioEnParrafo(document, mes, anio);

            // Obtener información de la demarcacion
            DemarcacionProjection demarcacionInfo = getDemarcacionInfo(tipo.toLowerCase());
            Integer demarcacionId = demarcacionInfo.getId();
            String demarcacionCodigo = demarcacionInfo.getCodigo();

            // Obtenemos los datos de escenario para las UTs de la demarcación para mes/año específicos
            List<IndicadorUTEscenarioProjection> listUTEscenario = indicadorUtEscasezRepository.findByAnioMesDemarcacionUTEscenario(anio, mes, demarcacionId);

            // Procesar y reemplazar los SVG en el documento
            String imgUTEs = DocumentWordUtils.procesarSVGFile('E', reportDir, listUTEscenario, demarcacionCodigo);

            // Insertar la imagen principal en el documento
            DocumentWordUtils.insertaImagenPrincipal(document, imgUTEs);
            DocumentWordUtils.insertarLeyendaImagen(document, "ESCENARIOS DE ESCASEZ");

            // Crear un párrafo vacío después de la imagen
            document.createParagraph();

            // Crear tabla de datos UT principal en el documento
            List<IndicadorDemarcacionFechaDataProjection> datos = datosDemarcacionUTEscasez(anio, tipo);
            DocumentWordUtils.crearTablaUT(document, datos);

            DocumentWordUtils.insertarLeyendaImagen(document, "INDICADORES DE ESCASEZ POR UTE ");

            // Configurar orientación horizontal DESPUÉS de la tabla
            XWPFParagraph paraHorizontal = document.createParagraph();
            DocumentWordUtils.configurarOrientacionHorizontal(paraHorizontal);

            DocumentWordUtils.configurarMargenes(paraHorizontal, 720, 720, 720, 720);
            DocumentWordUtils.agregarSaltoDePagina(paraHorizontal);


            UnidadTerritorialProjection utList = getUTsPorDemarcacionEscasez(demarcacionId).get(0); // TODO: Obtenemos la primera estación para probar

            // Agregar un margen superior
            document.createParagraph();

            // Siguiente contenido
            DocumentWordUtils.encabezadoH2(document, utList.getCodigo() + " - " + utList.getNombre());

            List<EstacionPesUtProjection> estacionesPesUt = pesUtEstacionRepository.findEstacionesPesIdWithCoeficienteByTipoAndUT('E', utList.getId());

            // Buscar la imagen correspondiente a la UTE y escenario actual del mes
            String pathImgUtActual = nombreImagenUTActual(demarcacionCodigo, listUTEscenario, utList);
            String tituloPeriodoActual = DateUtils.obtenerNombreMesCapitalizado(mes) + " - " + anio;

            // Crear la sección de contenido 1x2, tabla de estaciones UT e imagen de escenario
            DocumentWordUtils.crearContenido1x2(document, estacionesPesUt, pathImgUtActual, tituloPeriodoActual);


            // Obtener detalles de las estaciones por UT y año
            List<IndicadorUTFechaDataProjection> datosUTFecha = obtenerDatosUTFecha(utList.getId(), anio);
            List<IndicadorUTFechaDataProjection> totalesUTFecha = obtenerTotalesUTFecha(utList.getId(), anio);
            String escenario = getCurrentUTEscenario(utList.getId(), listUTEscenario);
            Double valorIndicador = getCurrentUTIndicadorTotal(utList.getId(), totalesUTFecha);
            DocumentWordUtils.insertarLeyendaTabla(document, 'E', anio, mes ,  valorIndicador, escenario);
            DocumentWordUtils.crearTablaDatosEstacionesUT(document, datosUTFecha, totalesUTFecha, utList.getNombre());

            // Comentario UT inferior
            String comentarioUT = getCurrentComentarioUT(utList.getId());
            DocumentWordUtils.insertarComentarioUt(document, comentarioUT);


//            System.out.println("Datos UT Fecha: " + datosUTFecha.size());
//            System.out.println("Totales UT Fecha: " + totalesUTFecha.size());

//            // Recorrer los datos para verificar imprimiendo en consola
//            System.out.println("Data:");
//            for (IndicadorUTFechaDataProjection data : datosUTFecha) {
//                System.out.println(data.getAnio() + " - " + data.getMes() + " - " + data.getId() + " - " + data.getCodigo() + " - " + data.getNombre() + " - " + data.getIndicador() + " - " + data.getValor());
//            }
//
//            System.out.println("Totales:");
//            for (IndicadorUTFechaDataProjection total : totalesUTFecha) {
//                System.out.println(total.getAnio() + " - " + total.getMes() + " - " + total.getId() + " - " + total.getCodigo() + " - " + total.getNombre() + " - " + total.getIndicador() + " - " + total.getValor());
//            }




            XWPFParagraph paraMargenesReducidos = document.createParagraph();
            DocumentWordUtils.configurarMargenes(paraMargenesReducidos, 720, 720, 720, 720);


            // Aquí continúa tu contenido en páginas verticales con márgenes reducidos
            DocumentWordUtils.encabezadoH2(document, "Siguiente UT");


            try (FileOutputStream out = new FileOutputStream(archivoFinal)) {
                document.write(out);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getCurrentComentarioUT(Integer utId) {
        return unidadTerritorialRepository.findComentarioById(utId);
    }

    private String getCurrentUTEscenario(Integer utId, List<IndicadorUTEscenarioProjection> listUTEscenario) {
        return listUTEscenario.stream()
                .filter(e -> e.getId().intValue() == utId)
                .map(IndicadorUTEscenarioProjection::getEscenarioFinal)
                .findFirst()
                .orElse("normalidad");
    }

    private Double getCurrentUTIndicadorTotal(Integer utId, List<IndicadorUTFechaDataProjection> totalesUTFecha) {
        return totalesUTFecha.stream()
                .filter(e -> e.getId().intValue() == utId)
                .map(IndicadorUTFechaDataProjection::getIndicador)
                .findFirst()
                .orElse(0.0);
    }

    private List<IndicadorUTFechaDataProjection> obtenerDatosUTFecha(Integer utId, Integer anio) {
        return reporteUtEscasezService.getUTEstacionFecha(utId, anio);
    }

    private List<IndicadorUTFechaDataProjection> obtenerTotalesUTFecha(Integer utId, Integer anio) {
        return reporteUtEscasezService.getTotalDataUTFecha(utId, anio);
    }

    private String nombreImagenUTActual(String demarcacionCodigo, List<IndicadorUTEscenarioProjection> listUTEscenario, UnidadTerritorialProjection utList) {
        String escenarioUt = listUTEscenario.stream()
                .filter(e -> e.getId().intValue() == utList.getId())
                .map(IndicadorUTEscenarioProjection::getEscenarioFinal)
                .findFirst()
                .orElse("normalidad");

        String nombreImagen = demarcacionCodigo + utList.getCodigo() + "-" + escenarioUt + ".png";
        return reportDir + "/imagenes-ut/imagenes-ute/" + nombreImagen;
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

    private List<UnidadTerritorialProjection> getUTsPorDemarcacionEscasez(Integer demarcacionId) {
        return unidadTerritorialRepository.findUnidadesTerritorialesByTipoDemarcacionAndPes('E', demarcacionId);
    }

}
