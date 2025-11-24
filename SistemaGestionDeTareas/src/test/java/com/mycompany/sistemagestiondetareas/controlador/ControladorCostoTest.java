package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.CostoDAO;
import com.mycompany.sistemagestiondetareas.modelo.Costo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ControladorCosto usando Mockito.
 */
public class ControladorCostoTest {

    private ControladorCosto controlador;
    private CostoDAO mockDao;

    @BeforeEach
    public void setUp() throws Exception {
        controlador = new ControladorCosto();
        mockDao = mock(CostoDAO.class);

        // Inyectar mock en el campo privado final costoDAO del controlador
        Field f = ControladorCosto.class.getDeclaredField("costoDAO");
        f.setAccessible(true);
        f.set(controlador, mockDao);
    }

    @Test
    public void registrarCosto_delegaEnDAO_yConstruyeCostoCorrectamente() {
        // Preparar mock: al insertar devolvemos el mismo objeto con id asignado
        when(mockDao.insertar(any(Costo.class))).thenAnswer(inv -> {
            Costo c = inv.getArgument(0);
            c.setId(99);
            return c;
        });

        String tipo = "PROYECTO";
        int idRef = 5;
        String descripcion = "Compra de licencias";
        double monto = 1500.75;
        String tipoCosto = "ADELANTO";
        int idUsuario = 2;

        Costo result = controlador.registrarCosto(tipo, idRef, descripcion, monto, tipoCosto, idUsuario);

        // Verificaciones sobre el objeto retornado
        assertNotNull(result);
        assertEquals(99, result.getId());
        assertEquals(tipo, result.getTipoReferencia());
        assertEquals(idRef, result.getIdReferencia());
        assertEquals(descripcion, result.getDescripcion());
        assertEquals(monto, result.getMonto(), 1e-9);
        assertEquals(tipoCosto, result.getTipoCosto());
        assertEquals(idUsuario, result.getIdUsuarioRegistro());
        assertNotNull(result.getFechaRegistro());

        // Verificamos que el DAO fue invocado exactamente 1 vez con el Costo construido
        ArgumentCaptor<Costo> captor = ArgumentCaptor.forClass(Costo.class);
        verify(mockDao, times(1)).insertar(captor.capture());
        Costo enviado = captor.getValue();

        assertEquals(tipo, enviado.getTipoReferencia());
        assertEquals(idRef, enviado.getIdReferencia());
        assertEquals(descripcion, enviado.getDescripcion());
        assertEquals(monto, enviado.getMonto(), 1e-9);
        assertEquals(tipoCosto, enviado.getTipoCosto());
        assertEquals(idUsuario, enviado.getIdUsuarioRegistro());
        assertNotNull(enviado.getFechaRegistro());
    }

    @Test
    public void obtenerCostosPorReferencia_delegaEnDAO_yRetornaLista() {
        // Preparar la respuesta del DAO
        Costo c1 = new Costo("PROYECTO", 7, "c1", 100.0, "RETRASO", new Date(), 1);
        Costo c2 = new Costo("PROYECTO", 7, "c2", 50.0, "ADELANTO", new Date(), 2);
        when(mockDao.listarPorReferencia("PROYECTO", 7)).thenReturn(Arrays.asList(c1, c2));

        List<Costo> res = controlador.obtenerCostosPorReferencia("PROYECTO", 7);
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("c1", res.get(0).getDescripcion());

        verify(mockDao, times(1)).listarPorReferencia("PROYECTO", 7);
    }

    @Test
    public void obtenerCostosPorUsuario_delegaEnDAO_yRetornaLista() {
        Costo c = new Costo("TAREA", 3, "c", 20.0, "GASTO_PLANIFICADO", new Date(), 9);
        when(mockDao.listarPorUsuario(9)).thenReturn(Arrays.asList(c));

        List<Costo> res = controlador.obtenerCostosPorUsuario(9);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(9, res.get(0).getIdUsuarioRegistro());

        verify(mockDao, times(1)).listarPorUsuario(9);
    }

    @Test
    public void calcularTotalPorTipo_delegaEnDAO() {
        when(mockDao.calcularTotalPorTipo("PROYECTO", 10, "ADELANTO")).thenReturn(200.0);

        double total = controlador.calcularTotalPorTipo("PROYECTO", 10, "ADELANTO");
        assertEquals(200.0, total, 1e-9);

        verify(mockDao, times(1)).calcularTotalPorTipo("PROYECTO", 10, "ADELANTO");
    }

    @Test
    public void calcularBalanceTotal_calculaCorrectamente() {
        // simulamos que el DAO devuelve estos totales
        when(mockDao.calcularTotalPorTipo("PROYECTO", 20, "ADELANTO")).thenReturn(300.0);
        when(mockDao.calcularTotalPorTipo("PROYECTO", 20, "RETRASO")).thenReturn(120.0);
        when(mockDao.calcularTotalPorTipo("PROYECTO", 20, "GASTO_PLANIFICADO")).thenReturn(30.0);

        double balance = controlador.calcularBalanceTotal("PROYECTO", 20);
        // formula: adelantos - retrasos - gastos
        assertEquals(300.0 - 120.0 - 30.0, balance, 1e-9);

        verify(mockDao, times(1)).calcularTotalPorTipo("PROYECTO", 20, "ADELANTO");
        verify(mockDao, times(1)).calcularTotalPorTipo("PROYECTO", 20, "RETRASO");
        verify(mockDao, times(1)).calcularTotalPorTipo("PROYECTO", 20, "GASTO_PLANIFICADO");
    }

    @Test
    public void registrarCosto_sinValidaciones_delController_pasaValoresInvalidosDirectoAlDAO() {
        // El controlador tal como está NO valida inputs (según tu código),
        // por tanto incluso un idReferencia <= 0 se pasará al DAO.
        when(mockDao.insertar(any(Costo.class))).thenReturn(null);

        Costo c = controlador.registrarCosto("NO_VALID", 0, "desc", -100.0, "TIPO_INCORRECTO", -1);

        // Dado que el DAO mock devuelve null, el resultado será null
        assertNull(c);

        // Aún así debe haberse llamado al DAO con los valores proporcionados
        ArgumentCaptor<Costo> captor = ArgumentCaptor.forClass(Costo.class);
        verify(mockDao).insertar(captor.capture());
        Costo enviado = captor.getValue();

        assertEquals("NO_VALID", enviado.getTipoReferencia());
        assertEquals(0, enviado.getIdReferencia());
        assertEquals(-100.0, enviado.getMonto(), 1e-9);
        assertEquals("TIPO_INCORRECTO", enviado.getTipoCosto());
        assertEquals(-1, enviado.getIdUsuarioRegistro());
    }
}
