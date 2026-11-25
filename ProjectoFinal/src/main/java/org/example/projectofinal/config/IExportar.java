package org.example.projectofinal.config;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.File;

public interface IExportar {

    void exportar(JasperPrint reporte, File destino)
            throws JRException;
}