package com.example.demo;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import model.Anime;
import model.Cancion;
import model.Contacto;


@Controller
public class PrimerController 
{
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public static boolean comprobarPassword(String password)
	{
		//Comprobar contraseña (Metodo de un usuario de internet)
		char clave;
		byte  contNumero = 0, contLetraMay = 0, contLetraMin=0;
		for (byte i = 0; i < password.length(); i++)
		{
			clave = password.charAt(i);
			String passValue = String.valueOf(clave);
			if (passValue.matches("[A-Z]")) {
				contLetraMay++;
			} else if (passValue.matches("[a-z]")) {
				contLetraMin++;
			} else if (passValue.matches("[0-9]")) {
				contNumero++;
			}
		}
		if(contLetraMin < 1 || contLetraMay < 1 || contNumero < 1)
		{
			return false;
		}
		else 
		{
			return true;
		}
	}
	
	public static boolean autentificacion(HttpServletRequest request, Model template) throws SQLException
	{
		HttpSession session = request.getSession();
		String autentificacion = (String) session.getAttribute("session");
		String username = (String) session.getAttribute("username");
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM usuarios WHERE username=?;");
		ps.setString(1, username);
		ResultSet resultado = ps.executeQuery();
		if(resultado.next() && session != null && autentificacion.equals(resultado.getString("session")) && username.equals(resultado.getString("username")))
		{
			template.addAttribute("blockedCuenta", "nav-item d-block");
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static void enviarCorreo(String de,String para, String mensaje, String asunto){
        Email from = new Email(de);
        String subject = asunto;
        Email to = new Email(para);
        Content content = new Content("text/plain", mensaje);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid("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");
        Request request = new Request();
        try {
          request.method = Method.POST;
          request.endpoint = "mail/send";
          request.body = mail.build();
          Response response = sg.api(request);
          System.out.println(response.statusCode);
          System.out.println(response.body);
          System.out.println(response.headers);
        } catch (IOException ex) {
          System.out.println(ex.getMessage()); ;
        }
    }
	
	// TODO: Terminar la subida de archivo y la validacion de tamaño de este
	
	
	@GetMapping("/")
	public static String paginaPrincipal(Model template, HttpServletRequest request) throws SQLException
	{
		Connection connection;
        connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes ORDER BY visitas DESC LIMIT 8;");
        ResultSet resultado = ps.executeQuery();
		ArrayList<Anime> listaHome;
		listaHome = new ArrayList<Anime>();
		
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		System.out.print(numeroSession);
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		template.addAttribute("titulo", "AnimeOps");
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
		}
		
		while(resultado.next())
		{
			Anime miAnime = new Anime(	resultado.getInt("id"),
					resultado.getString("nombre"),
					resultado.getString("sinopsis"),
					resultado.getString("genero1"),
					resultado.getString("genero2"),
					resultado.getString("genero3"),
					resultado.getString("tipo"),
					resultado.getString("imagen"),
					resultado.getInt("visitas"));
			listaHome.add(miAnime);
		}
		
		template.addAttribute("listaHome",listaHome);
		
		
		
        return "home";
	}
	
	@GetMapping("/animes/{idAnime}")
	public static String animeDelID(@PathVariable int idAnime, Model template,
										 HttpServletRequest request) throws SQLException
	{
		int v;
		String titulo;
		ArrayList<String> listaCanciones = new ArrayList<>();
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes WHERE id=?;");
		ps.setInt(1, idAnime);
		ResultSet resultado = ps.executeQuery();
		ResultSet resultado2;
		resultado.next();
		Anime animeDelID = new Anime(	resultado.getInt("id"),
				resultado.getString("nombre"),
				resultado.getString("sinopsis"),
				resultado.getString("genero1"),
				resultado.getString("genero2"),
				resultado.getString("genero3"),
				resultado.getString("tipo"),
				resultado.getString("imagen"),
				resultado.getInt("visitas"));
		titulo = animeDelID.getNombre();
		if(animeDelID.getGenero2() == null)
		{
			animeDelID.setGenero2("");
		}
		else {
			animeDelID.setGenero2(", " + animeDelID.getGenero2());
		}
		if(animeDelID.getGenero3() == null)
		{
			animeDelID.setGenero3("");
		}
		else {
			animeDelID.setGenero3(", " + animeDelID.getGenero3());
		}
		
		v = 1 + animeDelID.getVisitas();		
		ps = connection.prepareStatement("UPDATE animes SET visitas=? WHERE id=?;");
		ps.setInt(1, v);
		ps.setInt(2, animeDelID.getId());
		ps.executeUpdate();
		
		ps = connection.prepareStatement("SELECT tipo FROM canciones WHERE anime=? AND tipo LIKE 'OP%' GROUP BY tipo;");
		ps.setString(1, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		while(resultado2.next())
		{
			listaCanciones.add(resultado2.getString("tipo"));
		}
		
		ps = connection.prepareStatement("SELECT tipo FROM canciones WHERE anime=? AND tipo LIKE 'ED%' GROUP BY tipo;");
		ps.setString(1, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		while(resultado2.next())
		{
			listaCanciones.add(resultado2.getString("tipo"));
		}
		
		
		ps = connection.prepareStatement("SELECT tipo FROM canciones WHERE anime=? AND tipo LIKE 'OST' GROUP BY tipo;");
		ps.setString(1, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		if(resultado2.next())
		{
			listaCanciones.add(resultado2.getString("tipo"));
		}
		
		// TODO : Crear metodo y template especial para cada cacion mostrando 
		// todas las canciones subidas y por que usuario...
		
		template.addAttribute("archivo", animeDelID.getNombre() + " - ");
		template.addAttribute("listaCanciones",listaCanciones);
		template.addAttribute("titulo", titulo);
		template.addAttribute("animeDelID", animeDelID);
		
		//Login Autentificacion
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
		}
		// Fin de Autentificacion
		
		return "animeDelID";
	}
	
	@GetMapping("/canciones/{idCancion}")
	public static String descargarCancion(@PathVariable int idCancion) throws SQLException
	{
		Connection connection;
		String url = "/error";
		int descargas = 0;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM canciones WHERE id=?;");
		ps.setInt(1, idCancion);
		ResultSet resultado = ps.executeQuery();
		if(resultado.next())
		{
			url = resultado.getString("url");
			descargas = resultado.getInt("descargas");
			descargas++;
			ps = connection.prepareStatement("UPDATE canciones SET descargas=? WHERE id=?;");
			ps.setInt(1, descargas);
			ps.setInt(2, idCancion);
			ps.executeUpdate();
		}
		return "redirect:" + url;
	}
	
	@GetMapping("/animes/{idAnime}/{tipoCodigo}")
	public static String cancionDelID(@PathVariable int idAnime, Model template,
									  @PathVariable String tipoCodigo,
									  HttpServletRequest request) throws SQLException
	{
		int v;
		String titulo;
		ArrayList<String> listaCanciones = new ArrayList<>();
		ArrayList<Cancion> listaAportes;
		Cancion cancionAux;
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes WHERE id=?;");
		ps.setInt(1, idAnime);
		ResultSet resultado = ps.executeQuery();
		ResultSet resultado2;
		resultado.next();
		Anime animeDelID = new Anime(	resultado.getInt("id"),
				resultado.getString("nombre"),
				resultado.getString("sinopsis"),
				resultado.getString("genero1"),
				resultado.getString("genero2"),
				resultado.getString("genero3"),
				resultado.getString("tipo"),
				resultado.getString("imagen"),
				resultado.getInt("visitas"));
		titulo = animeDelID.getNombre();
		if(animeDelID.getGenero2() == null)
		{
			animeDelID.setGenero2("");
		}
		else {
			animeDelID.setGenero2(", " + animeDelID.getGenero2());
		}
		if(animeDelID.getGenero3() == null)
		{
			animeDelID.setGenero3("");
		}
		else {
			animeDelID.setGenero3(", " + animeDelID.getGenero3());
		}
		
		v = 1 + animeDelID.getVisitas();		
		ps = connection.prepareStatement("UPDATE animes SET visitas=? WHERE id=?;");
		ps.setInt(1, v);
		ps.setInt(2, animeDelID.getId());
		ps.executeUpdate();

		ps = connection.prepareStatement("SELECT tipo FROM canciones WHERE anime=? AND tipo LIKE 'OP%' GROUP BY tipo;");
		ps.setString(1, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		while(resultado2.next())
		{
			listaCanciones.add(resultado2.getString("tipo"));
		}
		
		ps = connection.prepareStatement("SELECT tipo FROM canciones WHERE anime=? AND tipo LIKE 'ED%' GROUP BY tipo;");
		ps.setString(1, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		while(resultado2.next())
		{
			listaCanciones.add(resultado2.getString("tipo"));
		}
		
		ps = connection.prepareStatement("SELECT tipo FROM canciones WHERE anime=? AND tipo LIKE 'OST' GROUP BY tipo;");
		ps.setString(1, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		if(resultado2.next())
		{
			listaCanciones.add(resultado2.getString("tipo"));
		}
		
		ps = connection.prepareStatement("SELECT * FROM canciones WHERE tipo=? AND anime=? ORDER BY descargas DESC;");
		ps.setString(1, tipoCodigo);
		ps.setString(2, animeDelID.getNombre());
		resultado2 = ps.executeQuery();
		
		listaAportes = new ArrayList<>();
		
		while(resultado2.next())
		{
			cancionAux = new Cancion(resultado2.getInt("id"), resultado2.getString("nombre"),
					resultado2.getString("tipo"), resultado2.getString("banda"),
					resultado2.getString("anime"), resultado2.getInt("descargas"),
					resultado2.getString("usuario"), resultado2.getString("url"));
			
			listaAportes.add(cancionAux);
		}
		
		template.addAttribute("listaAportes", listaAportes);
		template.addAttribute("archivo", animeDelID.getNombre() + " - ");
		template.addAttribute("listaCanciones",listaCanciones);
		template.addAttribute("titulo", titulo);
		template.addAttribute("animeDelID", animeDelID);
		
		//Login Autentificacion
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
		}
		// Fin de Autentificacion
		
		template.addAttribute("titulo", tipoCodigo + " - " + animeDelID.getNombre());
		
		return "cancionDelID";
	}
	
	@PostMapping("/busqueda")
	public static String paginaRedirectBusqueda(@RequestParam String busqueda,
										Model template, HttpServletRequest request)
	{
		return "redirect:/busqueda/" + busqueda;
	}
	
	@GetMapping("/busqueda/{busqueda}")
	public static String paginaBusqueda(@PathVariable String busqueda,
										Model template, HttpServletRequest request) throws SQLException
	{
		boolean result = false;
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes WHERE UPPER(nombre) LIKE UPPER(?);");
		ps.setString(1, "%" + busqueda + "%");
		ResultSet resultado = ps.executeQuery();
		ArrayList<Anime> listaResultados = new ArrayList<Anime>();
		
		while(resultado.next())
		{
			result = true;
			Anime resultados = new Anime(	resultado.getInt("id"),
					resultado.getString("nombre"),
					resultado.getString("sinopsis"),
					resultado.getString("genero1"),
					resultado.getString("genero2"),
					resultado.getString("genero3"),
					resultado.getString("tipo"),
					resultado.getString("imagen"),
					resultado.getInt("visitas"));
			
			listaResultados.add(resultados);
		}
		
		//Login Autentificacion
				HttpSession session = request.getSession();
				String numeroSession = (String) session.getAttribute("session");
				String nombreUsuario;
				PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
				ps2.setString(1, numeroSession);
				ResultSet result2 = ps2.executeQuery();
				if(autentificacion(request,template) && result2.next())
				{
					nombreUsuario = result2.getString("username");
					template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
					template.addAttribute("registro", "Logout");
					template.addAttribute("loginLink", "/cuenta");
					template.addAttribute("registroLink", "/logout");
				}else
				{
					template.addAttribute("login", "Login");
					template.addAttribute("registro", "Registrarse");
					template.addAttribute("loginLink", "/login");
					template.addAttribute("registroLink", "registro");
				}
		// Fin de Autentificacion
		
		if(result)
		{
			template.addAttribute("busqueda", busqueda);
			template.addAttribute("listaResultados", listaResultados);
			template.addAttribute("titulo", "Resultados de: " + busqueda);
			return "busquedaResultado";
		}
		else
		{
			template.addAttribute("busqueda", busqueda);
			template.addAttribute("titulo", busqueda + " - No hay Resultados");
			return "busquedaResultadoVacio";
		}
	}
	
	@GetMapping("/logout")
	public static String logoutCuenta(HttpServletRequest request,Model template)
	{
		HttpSession session = request.getSession();
		session.setAttribute("session", "");
		return "redirect:/";
	}
	
	@GetMapping("/editar")
	public static String paginaAdmin(HttpServletRequest request, Model template) throws SQLException
	{
		if(autentificacion(request,template))
		{
			return "admin";
		}
		else
		{
			return "redirect:/login";
		}
	}
	
	@GetMapping("/registro")
	public static String paginaRegistro(Model template)
	{
		template.addAttribute("titulo", "Registrarse");
		return "registro";
	}
	
	@PostMapping("/registro")
	public static String paginaRegistrar(@RequestParam String username,
										 @RequestParam String email,
										 @RequestParam String password,
										 @RequestParam String password_confirm,
										 Model template) throws SQLException
	{
		boolean correctUsername = true, correctEmail = true, correctPassword = true, comprobar2;
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM usuarios WHERE username=?;");
		ps.setString(1, username);
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE email=?;");
		ps2.setString(1, email);
		ResultSet resultado2 = ps2.executeQuery();
		ResultSet resultado = ps.executeQuery();
		if(resultado.next())
		{
			template.addAttribute("mensajeErrorUsername","Error: Nombre de usuario no disponible");
			template.addAttribute("errorUsername","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			correctUsername = false;
		}else if(username.isEmpty())
		{
			template.addAttribute("mensajeErrorUsername","Error: Coloque un Nombre de Usuario");
			template.addAttribute("errorUsername","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			
			correctUsername = false;
		}
		if(resultado2.next())
		{
			template.addAttribute("mensajeErrorEmail","Error: Email no disponible");
			template.addAttribute("errorEmail","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			
			correctEmail = false;
		}else if(email.isEmpty())
		{
			template.addAttribute("mensajeErrorEmail","Error: Coloque una direccion de Email");
			template.addAttribute("errorEmail","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			
			correctEmail = false;
		}
		if(!comprobarPassword(password))
		{
			template.addAttribute("mensajeErrorPassword","Error: La Contraseña debe tener una Letra Mayuscula, una Minuscula y un Numero");
			template.addAttribute("errorPassword","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			
			correctPassword = false;
		}else if(!password.equals(password_confirm))
		{
			template.addAttribute("mensajeErrorPassword","Error: Las Contraseñas no coinciden");
			template.addAttribute("errorPassword","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			
			correctPassword = false;
		}else if(password.isEmpty())
		{
			template.addAttribute("mensajeErrorPassword","Error: Coloque una Contraseña");
			template.addAttribute("errorPassword","alert alert-danger small");
			template.addAttribute("antesUsername",username);
			template.addAttribute("antesEmail",email);
			
			correctPassword = false;
		}
		if(!correctUsername || !correctPassword || !correctEmail)
		{
			template.addAttribute("titulo", "Registrarse");
			return "registro";
		}else
		{
			Random random = new Random();
			int i; String session;
			PreparedStatement registrar = connection.prepareStatement("INSERT INTO usuarios(username,password,session,email) VALUES(?,?,?,?);");
			registrar.setString(1, username);
			registrar.setString(2, password);
			registrar.setString(4, email);
			PreparedStatement comprobarSession = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
			
			do
			{
				i = random.nextInt();
				if(i < 0)
				{
					i = i*(-1);
				}
				session = "" + i;
				comprobarSession.setString(1, session);
				ResultSet comprobar = comprobarSession.executeQuery();
				comprobar2 = comprobar.next();
			}while(comprobar2);
			
			registrar.setString(3, session);
			registrar.executeUpdate();
			
			
			return "redirect:/login";
		}
	}
	
	@GetMapping("/login")
	public static String loginCuenta(Model template)
	{
		template.addAttribute("titulo", "Login");
		return "login";
	}
	
	@PostMapping("/login")
	public static String loginCuentaAutentificacion(	@RequestParam String usuario, 
													@RequestParam String contrasena,
													HttpServletRequest request,
													Model template) throws SQLException
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM usuarios WHERE username=?;");
        ps.setString(1, usuario);
		ResultSet resultado = ps.executeQuery();
		if(resultado.next())
		{
			if(contrasena.equals(resultado.getString("password")))
			{
				HttpSession session = request.getSession();
				session.setAttribute("session", resultado.getString("session"));
				session.setAttribute("username", resultado.getString("username"));
				return "redirect:/cuenta";
			}else
			{
				template.addAttribute("mensajeError","Error: Nombre y Contraseña no coinciden");
				template.addAttribute("error","alert alert-danger small");
				template.addAttribute("titulo", "Login");
				return "login";
			}
		}else
		{
			template.addAttribute("mensajeError","Error: Nombre y Contraseña no coinciden");
			template.addAttribute("error","alert alert-danger small");
			template.addAttribute("titulo", "Login");
			return "login";
		}
	}
	
	/*
	@GetMapping("/sucursales")
	public static String paginaSucursales(Model template)
	{
		template.addAttribute("claseSucursales","active");
		return "sucursales";
	}
	*/
	@GetMapping("/animes")
	public static String paginaAnimes(Model template, HttpServletRequest request) throws SQLException
	{
		Connection connection;
        connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes ORDER BY nombre;");
        
		ResultSet resultado = ps.executeQuery();
		ArrayList<Anime> listaAnimes;
		listaAnimes = new ArrayList<Anime>();
		
		while(resultado.next())
		{
			Anime miAnime = new Anime(	resultado.getInt("id"),
					resultado.getString("nombre"),
					resultado.getString("sinopsis"),
					resultado.getString("genero1"),
					resultado.getString("genero2"),
					resultado.getString("genero3"),
					resultado.getString("tipo"),
					resultado.getString("imagen"),
					resultado.getInt("visitas"));
			listaAnimes.add(miAnime);
		}
		
		template.addAttribute("listaanimes",listaAnimes);
		
		//Login Autentificacion
				HttpSession session = request.getSession();
				String numeroSession = (String) session.getAttribute("session");
				String nombreUsuario;
				PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
				ps2.setString(1, numeroSession);
				ResultSet result = ps2.executeQuery();
				if(autentificacion(request,template) && result.next())
				{
					nombreUsuario = result.getString("username");
					template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
					template.addAttribute("registro", "Logout");
					template.addAttribute("loginLink", "/cuenta");
					template.addAttribute("registroLink", "/logout");
				}else
				{
					template.addAttribute("login", "Login");
					template.addAttribute("registro", "Registrarse");
					template.addAttribute("loginLink", "/login");
					template.addAttribute("registroLink", "registro");
				}
				// Fin de Autentificacion
				
		template.addAttribute("titulo", "Todos los Animes");	
        return "animes";
	}
	
	
	
	
	@GetMapping("/cuenta")
	public static String paginaCuenta(Model template, HttpServletRequest request) throws SQLException 
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		
		//Login Autentificacion
		template.addAttribute("Text", "text-align: center; font-size: 20px;");
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("titulo", "Tu Cuenta");
			return "cuenta";
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
			return "redirect:/login";
		}
		// Fin de Autentificacion
	}

	@GetMapping("/cuenta/perfil")
	public static String paginaCuentaPerfil(Model template, HttpServletRequest request) throws SQLException 
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		
		//Login Autentificacion
		template.addAttribute("Text", "text-align: center; font-size: 20px;");
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("perfilActivo", "active");
			template.addAttribute("nombrePerfil", result.getString("username"));
			template.addAttribute("emailPerfil", result.getString("email"));
			template.addAttribute("titulo", "Tu Cuenta - Perfil");
			return "perfil";
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
			return "redirect:/login";
		}
		// Fin de Autentificacion
	}
	
	@GetMapping("/borrar/{cancionID}")
	public static String borrarCancion(@PathVariable int cancionID, HttpServletRequest request, Model template) throws SQLException
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		
		//Login Autentificacion
		template.addAttribute("Text", "text-align: center; font-size: 20px;");
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario,nombreUsuario2;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		PreparedStatement ps = connection.prepareStatement("DELETE FROM canciones WHERE id=?;");
		ps.setInt(1, cancionID);
		PreparedStatement ps3 = connection.prepareStatement("SELECT * FROM canciones WHERE id=?;");
		ps3.setInt(1, cancionID);
		ResultSet resultado = ps3.executeQuery();
		if(autentificacion(request,template) && result.next() && resultado.next())
		{
			nombreUsuario2 = resultado.getString("usuario");
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("perfilActivo", "active");
			template.addAttribute("nombrePerfil", result.getString("username"));
			template.addAttribute("emailPerfil", result.getString("email"));
			
			if(nombreUsuario.equals(nombreUsuario2))
			{
				ps.executeUpdate();
				return "redirect:/cuenta/aportes";
			}else
			{
				return "redirect:/";
			}
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
			return "redirect:/login";
		}
		// Fin de Autentificacion
		
	}
	
	@GetMapping("/cuenta/aportes")
	public static String paginaCuentaAportes(Model template, HttpServletRequest request) throws SQLException
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);	
		//Login Autentificacion
		template.addAttribute("Text", "text-align: center; font-size: 20px;");
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			ArrayList<Cancion> listaAportes;
			listaAportes = new ArrayList<Cancion>();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM canciones WHERE usuario=? ORDER BY anime;");
			ps.setString(1, nombreUsuario);
			ResultSet resultado = ps.executeQuery();
			Cancion cancionAux;
			while(resultado.next())
			{
				cancionAux = new Cancion(resultado.getInt("id"), resultado.getString("nombre"),
						resultado.getString("tipo"), resultado.getString("banda"),
						resultado.getString("anime"), resultado.getInt("descargas"),
						resultado.getString("usuario"), resultado.getString("url"));
				listaAportes.add(cancionAux);
			}
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("perfilActivo", "active");
			template.addAttribute("nombrePerfil", result.getString("username"));
			template.addAttribute("emailPerfil", result.getString("email"));
			template.addAttribute("listaAportes",listaAportes);
			template.addAttribute("titulo", "Tu Cuenta - Tus Aportes");
			return "aportes";
		}else
		{
			template.addAttribute("login", "Login");
			template.addAttribute("registro", "Registrarse");
			template.addAttribute("loginLink", "/login");
			template.addAttribute("registroLink", "registro");
			return "redirect:/login";
		}
		// Fin de Autentificacion
		
	}
	
	@GetMapping("/añadir")
	public static String PaginaAñadir(Model template,HttpServletRequest request) throws SQLException
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes ORDER BY nombre;");
        
		ResultSet resultado = ps.executeQuery();
		ArrayList<Anime> listaAnimes;
		listaAnimes = new ArrayList<Anime>();
		
		while(resultado.next())
		{
			Anime miAnime = new Anime(	resultado.getInt("id"),
					resultado.getString("nombre"),
					resultado.getString("sinopsis"),
					resultado.getString("genero1"),
					resultado.getString("genero2"),
					resultado.getString("genero3"),
					resultado.getString("tipo"),
					resultado.getString("imagen"),
					resultado.getInt("visitas"));
			listaAnimes.add(miAnime);
		}
		
		template.addAttribute("listaanimes",listaAnimes);
		
		
		//Login Autentificacion
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("titulo", "Añadir una Cancion");
			return "añadir";
		}else
		{
			return "redirect:/login";
		}
		// Fin de Autentificacion
	}
	
	@GetMapping("/enviarComentario")
	public static String enviarComentario(HttpServletRequest request, Model template) throws SQLException
	{
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		//Login Autentificacion
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/cuenta");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("Text", "text-align: center; font-size: 20px;");
			template.addAttribute("titulo", "Ayudanos a Mejorar!");
			return "enviarComentario";
		}else
		{
			template.addAttribute("titulo", "Ayudanos a Mejorar!");
			return "enviarComentarioSinLoguear";
		}
		// Fin de Autentificacion	
	}
	
	@PostMapping("/enviarComentario")
	public static String enviarComentarioProceso(@RequestParam String sugerencia,
												 @RequestParam String categoria, 
												 @RequestParam String nombre,
												 @RequestParam String email,
												 HttpServletRequest request,
												 Model template) throws SQLException
	{
		HttpSession session = request.getSession();
		boolean cNombre = true, cSugerencia = true, cCategoria = true;
		Connection connection;
		String numeroSession = (String) session.getAttribute("session"),nombreUsuario,emailUsuario;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps.setString(1, numeroSession);
		ResultSet resultado = ps.executeQuery();
		ps = connection.prepareStatement("INSERT INTO mensajes(usuario,categoria,sugerencia,email) VALUES(?,?,?,?)");
		if(resultado.next())
		{
			if(sugerencia.isEmpty())
			{
				cSugerencia = false;
				template.addAttribute("mensajeErrorSugerencia","Error: Escriba un Mensaje");
				template.addAttribute("errorSugerencia","alert alert-danger small");
			}
			if(categoria.equals("error"))
			{
				cCategoria = false;
				template.addAttribute("mensajeErrorCategoria","Error: Eliga una Categoria");
				template.addAttribute("errorCategoria","alert alert-danger small");
				template.addAttribute("antesSugerencia",sugerencia);
			}
			if(cCategoria && cSugerencia)
			{
				nombreUsuario = resultado.getString("username");
				emailUsuario = resultado.getString("email");
				ps.setString(1, nombreUsuario);
				ps.setString(2, categoria);
				ps.setString(3, sugerencia);
				ps.setString(4, emailUsuario);
				ps.executeUpdate();
				return "redirect:/";
			}
			else
			{
				nombreUsuario = resultado.getString("username");
				template.addAttribute("login", "Bienvenido/a, " + nombreUsuario);
				template.addAttribute("registro", "Logout");
				template.addAttribute("loginLink", "/cuenta");
				template.addAttribute("registroLink", "/logout");
				template.addAttribute("Text", "text-align: center; font-size: 20px;");
				template.addAttribute("titulo", "Ayudanos a Mejorar!");
				return "enviarComentario";
			}
			
		}else
		{
			if(nombre.isEmpty())
			{
				cNombre = false;
				template.addAttribute("mensajeErrorNombre","Error: Coloque un Nombre");
				template.addAttribute("errorNombre","alert alert-danger small");
				template.addAttribute("antesEmail",email);
				template.addAttribute("antesSugerencia",sugerencia);
			}
			if(sugerencia.isEmpty())
			{
				cSugerencia = false;
				template.addAttribute("mensajeErrorSugerencia","Error: Escriba un Mensaje");
				template.addAttribute("errorSugerencia","alert alert-danger small");
				template.addAttribute("antesNombre",nombre);
				template.addAttribute("antesEmail",email);
			}
			if(categoria.equals("error"))
			{
				cCategoria = false;
				template.addAttribute("mensajeErrorCategoria","Error: Eliga una Categoria");
				template.addAttribute("errorCategoria","alert alert-danger small");
				template.addAttribute("antesNombre",nombre);
				template.addAttribute("antesEmail",email);
				template.addAttribute("antesSugerencia",sugerencia);
			}
			
			if(cCategoria && cNombre && cSugerencia)
			{
				ps.setString(1, nombre + " (No registrado)");
				ps.setString(2, categoria);
				ps.setString(3, sugerencia);
				if(email.isEmpty())
				{
					email = "(No especificado)";
				}
				ps.setString(4, email);
				ps.executeUpdate();
				return "redirect:/";
			}
			else
			{
				template.addAttribute("titulo", "Ayudanos a Mejorar!");
				return "enviarComentarioSinLoguear";
			}
		}
	}
	
	@PostMapping("/añadir")
	public static String procesarAñadir(@RequestParam String cancionNombre,
										@RequestParam String tipo,
										@RequestParam String anime, HttpServletRequest request,
										@RequestParam String banda, Model template,
										@RequestParam int numeroTipo,
										@RequestParam String url) throws SQLException
	{
		boolean correctCancion = true, archivo = true, correctTipo = true, correctAnime = true, correctBanda = true;
		Connection connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		//Login Autentificacion	
		HttpSession session = request.getSession();
		String numeroSession = (String) session.getAttribute("session");
		String nombreUsuario;
		PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM usuarios WHERE session=?;");
		ps2.setString(1, numeroSession);
		ResultSet result = ps2.executeQuery();
		if(autentificacion(request,template) && result.next())
		{
			if(url.isEmpty())
			{
				archivo = false;
				template.addAttribute("mensajeErrorArchivo", "Error: Debe Seleccionar un archivo haciendo click en \"Subir Archivo\"");
				template.addAttribute("errorArchivo","alert alert-danger small");
			}
			if(cancionNombre.isEmpty())
			{
				correctCancion = false;
				template.addAttribute("mensajeErrorNombre","Error: Este campo es Obligatorio");
				template.addAttribute("errorNombre","alert alert-danger small");
				template.addAttribute("bandaAnterior", banda);
				
			}
			if(tipo.equals("error"))
			{
				correctTipo = false;
				template.addAttribute("mensajeErrorTipo","Error: Este campo es Obligatorio");
				template.addAttribute("errorTipo","alert alert-danger small");
				
			}
			if(anime.equals("error"))
			{
				correctAnime = false;
				template.addAttribute("mensajeErrorAnime","Error: Este campo es Obligatorio");
				template.addAttribute("errorAnime","alert alert-danger small");
			}
			if(banda.isEmpty())
			{
				correctBanda = false;
				template.addAttribute("mensajeErrorBanda","Error: Este campo es Obligatorio");
				template.addAttribute("errorBanda","alert alert-danger small");
				template.addAttribute("nombreAnterior", cancionNombre);
				
			}
			if(correctAnime && correctBanda && correctCancion && correctTipo && archivo)
			{
				String tipo2 = tipo + numeroTipo;
				PreparedStatement añadir = connection.prepareStatement("INSERT INTO canciones(nombre,tipo,banda,anime,usuario,url) VALUES(?,?,?,?,?,?);");
				añadir.setString(1, cancionNombre);
				if(tipo.equals("OST"))
				{
					añadir.setString(2, tipo);
				}else
				{
					añadir.setString(2, tipo2);
				}
				añadir.setString(3, banda);
				añadir.setString(4, anime);
				nombreUsuario = result.getString("username");
				añadir.setString(5, nombreUsuario);
				añadir.setString(6, url);
				añadir.executeUpdate();
				return "redirect:/";
			}else
			{
				PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes ORDER BY nombre;");
				ResultSet resultado = ps.executeQuery();
				ArrayList<Anime> listaAnimes;
				listaAnimes = new ArrayList<Anime>();
				
				while(resultado.next())
				{
					Anime miAnime = new Anime(	resultado.getInt("id"),
							resultado.getString("nombre"),
							resultado.getString("sinopsis"),
							resultado.getString("genero1"),
							resultado.getString("genero2"),
							resultado.getString("genero3"),
							resultado.getString("tipo"),
							resultado.getString("imagen"),
							resultado.getInt("visitas"));
					listaAnimes.add(miAnime);
				}
				
				template.addAttribute("listaanimes",listaAnimes);
				template.addAttribute("titulo", "Añadir una Cancion");
				
				return "añadir";
			}
		}else
		{
			return "redirect:/login";
		}
		// Fin de Autentificacion
	}	
}
