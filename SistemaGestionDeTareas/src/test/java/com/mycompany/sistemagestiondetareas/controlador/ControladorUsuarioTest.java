package com.mycompany.sistemagestiondetareas.controlador;

import com.mycompany.sistemagestiondetareas.dao.UsuarioDAO;
import com.mycompany.sistemagestiondetareas.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorUsuarioTest {

    private ControladorUsuario controlador;
    private UsuarioDAO mockDAO;

    @BeforeEach
    public void setUp() throws Exception {
        controlador = new ControladorUsuario();
        mockDAO = mock(UsuarioDAO.class);

        // Inyectar el mock en el controlador usando reflection
        Field fDao = ControladorUsuario.class.getDeclaredField("usuarioDAO");
        fDao.setAccessible(true);
        fDao.set(controlador, mockDAO);

        // Para evitar que verificarDatosIniciales cree usuarios automáticamente
        when(mockDAO.listarTodos()).thenReturn(Collections.emptyList());
    }

    // ----------------------------------------------------
    //   VERIFICAR DATOS INICIALES (TEST CORREGIDO)
    // ----------------------------------------------------

    @Test
    public void constructor_creaUsuariosInicialesCuandoNoHay() throws Exception {
        // Configurar mock para simular BD vacía
        when(mockDAO.listarTodos()).thenReturn(Collections.emptyList());
        when(mockDAO.buscarPorEmail(anyString())).thenReturn(null);
        when(mockDAO.insertar(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1);
            return u;
        });
        
        // Llamar directamente al método privado usando reflexión
        Method method = ControladorUsuario.class.getDeclaredMethod("verificarDatosIniciales");
        method.setAccessible(true);
        method.invoke(controlador);
        
        // Verificar que se intentó insertar 2 usuarios (admin y regular)
        verify(mockDAO, times(2)).insertar(any());
    }

    // ----------------------------------------------------
    //   REGISTRAR USUARIO
    // ----------------------------------------------------

    @Test
    public void registrarUsuario_datosInvalidos_retornaNull() {
        assertNull(controlador.registrarUsuario("", "Apellido", "mail", "123", false));
        assertNull(controlador.registrarUsuario("Nom", "", "mail", "123", false));
        assertNull(controlador.registrarUsuario("Nom", "Ape", "", "123", false));
        assertNull(controlador.registrarUsuario("Nom", "Ape", "mail", "", false));
        verify(mockDAO, never()).insertar(any());
    }

    @Test
    public void registrarUsuario_emailDuplicado_retornaNull() {
        when(mockDAO.buscarPorEmail("test@mail.com"))
                .thenReturn(new Usuario("A", "B", "test@mail.com", "123", false));

        assertNull(controlador.registrarUsuario("Nom", "Ape", "test@mail.com", "123", false));
        verify(mockDAO, never()).insertar(any());
    }

    @Test
    public void registrarUsuario_datosValidos_retornaUsuario() {
        when(mockDAO.buscarPorEmail("test@mail.com")).thenReturn(null);

        Usuario mockResp = new Usuario("Nom", "Ape", "test@mail.com", "123", false);
        mockResp.setId(10);

        when(mockDAO.insertar(any())).thenReturn(mockResp);

        Usuario u = controlador.registrarUsuario("Nom", "Ape", "test@mail.com", "123", false);

        assertNotNull(u);
        assertEquals(10, u.getId());
    }

    // ----------------------------------------------------
    //   AUTENTICAR
    // ----------------------------------------------------

    @Test
    public void autenticarUsuario_credencialesCorrectas() {
        Usuario u = new Usuario("A", "B", "mail@mail.com", "pass", false);
        when(mockDAO.buscarPorEmail("mail@mail.com")).thenReturn(u);

        Usuario auth = controlador.autenticarUsuario("mail@mail.com", "pass");

        assertNotNull(auth);
        assertEquals("mail@mail.com", auth.getEmail());
    }

    @Test
    public void autenticarUsuario_credencialesIncorrectas() {
        Usuario u = new Usuario("A", "B", "mail@mail.com", "pass", false);
        when(mockDAO.buscarPorEmail("mail@mail.com")).thenReturn(u);

        assertNull(controlador.autenticarUsuario("mail@mail.com", "wrong"));
    }

    @Test
    public void autenticarUsuario_emailNoExiste() {
        when(mockDAO.buscarPorEmail("mail@mail.com")).thenReturn(null);
        assertNull(controlador.autenticarUsuario("mail@mail.com", "pass"));
    }

    // ----------------------------------------------------
    //   OBTENER USUARIO
    // ----------------------------------------------------

    @Test
    public void obtenerUsuarioPorId() {
        Usuario u = new Usuario("A", "B", "m@m.com", "1", false);
        u.setId(7);

        when(mockDAO.buscarPorId(7)).thenReturn(u);

        Usuario res = controlador.obtenerUsuarioPorId(7);
        assertNotNull(res);
        assertEquals(7, res.getId());
    }

    @Test
    public void obtenerTodosLosUsuarios() {
        when(mockDAO.listarTodos()).thenReturn(Arrays.asList(
                new Usuario("A", "B", "1@a.com", "1", false),
                new Usuario("C", "D", "2@b.com", "2", false)
        ));

        List<Usuario> lista = controlador.obtenerTodosLosUsuarios();
        assertEquals(2, lista.size());
    }

    // ----------------------------------------------------
    //   ACTUALIZAR USUARIO
    // ----------------------------------------------------

    @Test
    public void actualizarUsuario_datosInvalidos_retornaFalse() {
        assertFalse(controlador.actualizarUsuario(null));

        Usuario u = new Usuario("A", "B", "m@m.com", "1", false);
        u.setId(0);

        assertFalse(controlador.actualizarUsuario(u));
        verify(mockDAO, never()).actualizar(any());
    }

    @Test
    public void actualizarUsuario_valido() {
        Usuario u = new Usuario("A", "B", "m@m.com", "1", false);
        u.setId(5);

        when(mockDAO.actualizar(u)).thenReturn(true);

        assertTrue(controlador.actualizarUsuario(u));
        verify(mockDAO, times(1)).actualizar(u);
    }

    // ----------------------------------------------------
    //   ACTUALIZAR PASSWORD
    // ----------------------------------------------------

    @Test
    public void actualizarPassword_usuarioNoExiste_retornaFalse() {
        when(mockDAO.buscarPorId(10)).thenReturn(null);
        assertFalse(controlador.actualizarPassword(10, "nueva"));
    }

    @Test
    public void actualizarPassword_datosInvalidos_retornaFalse() {
        Usuario u = new Usuario("A", "B", "m@m.com", "1", false);
        u.setId(10);

        when(mockDAO.buscarPorId(10)).thenReturn(u);

        assertFalse(controlador.actualizarPassword(10, ""));
        verify(mockDAO, never()).actualizar(any());
    }

    @Test
    public void actualizarPassword_valido() {
        Usuario u = new Usuario("A", "B", "m@m.com", "1", false);
        u.setId(10);

        when(mockDAO.buscarPorId(10)).thenReturn(u);
        when(mockDAO.actualizar(any())).thenReturn(true);

        assertTrue(controlador.actualizarPassword(10, "nueva123"));
        verify(mockDAO, times(1)).actualizar(any());
    }

    // ----------------------------------------------------
    //   ELIMINAR
    // ----------------------------------------------------

    @Test
    public void eliminarUsuario_invalido() {
        assertFalse(controlador.eliminarUsuario(0));
        verify(mockDAO, never()).eliminar(anyInt());
    }

    @Test
    public void eliminarUsuario_valido() {
        when(mockDAO.eliminar(5)).thenReturn(true);
        assertTrue(controlador.eliminarUsuario(5));
        verify(mockDAO, times(1)).eliminar(5);
    }
}