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
		if(autentificacion(request,template) && result.next())
		{
			nombreUsuario = result.getString("username");
			template.addAttribute("login", "Bienvenido, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/editar");
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
		Cancion cancionAux;
		ArrayList<Cancion> listaCanciones = new ArrayList<>();
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
		
		ps = connection.prepareStatement("SELECT * FROM canciones WHERE anime=?;");
		ps.setString(1, animeDelID.getNombre());
		
		resultado2 = ps.executeQuery();
		
		while(resultado2.next())
		{
			cancionAux = new Cancion(resultado2.getInt("id"), resultado2.getString("nombre"),
									resultado2.getString("tipo"), resultado2.getString("banda"),
									resultado2.getString("anime"), resultado2.getInt("descargas"),
									resultado2.getString("usuario"));
			listaCanciones.add(cancionAux);
			
		}
		
		
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
			template.addAttribute("login", "Bienvenido, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/editar");
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
	
	@PostMapping("/busqueda")
	public static String paginaBusqueda(@RequestParam String busqueda,
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
					template.addAttribute("login", "Bienvenido, " + nombreUsuario);
					template.addAttribute("registro", "Logout");
					template.addAttribute("loginLink", "/editar");
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
			return "busquedaResultado";
		}
		else
		{
			return "busquedaResultadoVacio";
		}
	}
	
	@GetMapping("/mensajes")
	public static String paginaMensajes(Model template) throws SQLException
	{
		/*String mensajeNombre;
		String mensajeEmail;
		String mensajeComentario;
		*/
		Contacto contacto;
		
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);	
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM mensajes");
		ResultSet resultado = ps.executeQuery();
		ArrayList<Contacto> listaMensajes;
		listaMensajes = new ArrayList<Contacto>();
		
		while(resultado.next())
		{
			contacto = new Contacto(resultado.getInt("id"), 
					resultado.getString("nombre"),
					resultado.getString("email"),
					resultado.getString("comentario"));
		/*	mensajeNombre = resultado.getString("nombre");
			mensajeEmail = resultado.getString("email");
			mensajeComentario = resultado.getString("comentario");
			
			*/
			listaMensajes.add(contacto);
		}
		
		template.addAttribute("listaMensajes", listaMensajes);
		
		
		return "mensajesDeContacto";
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
	public static String paginaRegistro()
	{
		
		
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
	public static String loginCuenta()
	{
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
				return "login";
			}
		}else
		{
			template.addAttribute("mensajeError","Error: Nombre y Contraseña no coinciden");
			template.addAttribute("error","alert alert-danger small");
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
					template.addAttribute("login", "Bienvenido, " + nombreUsuario);
					template.addAttribute("registro", "Logout");
					template.addAttribute("loginLink", "/editar");
					template.addAttribute("registroLink", "/logout");
				}else
				{
					template.addAttribute("login", "Login");
					template.addAttribute("registro", "Registrarse");
					template.addAttribute("loginLink", "/login");
					template.addAttribute("registroLink", "registro");
				}
				// Fin de Autentificacion
				
		
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
			template.addAttribute("login", "Bienvenido, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/editar");
			template.addAttribute("registroLink", "/logout");
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
			template.addAttribute("login", "Bienvenido, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/editar");
			template.addAttribute("registroLink", "/logout");
			template.addAttribute("perfilActivo", "active");
			template.addAttribute("nombrePerfil", result.getString("username"));
			template.addAttribute("emailPerfil", result.getString("email"));
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
			template.addAttribute("login", "Bienvenido, " + nombreUsuario);
			template.addAttribute("registro", "Logout");
			template.addAttribute("loginLink", "/editar");
			template.addAttribute("registroLink", "/logout");
			return "añadir";
		}else
		{
			return "redirect:/login";
		}
		// Fin de Autentificacion
	}
	
	@PostMapping("/añadir")
	public static String procesarAñadir(@RequestParam String cancionNombre,
										@RequestParam String tipo,
										@RequestParam String anime, HttpServletRequest request,
										@RequestParam String banda, Model template) throws SQLException
	{
		boolean correctCancion = true, correctTipo = true, correctAnime = true, correctBanda = true;
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
			if(correctAnime && correctBanda && correctCancion && correctTipo)
			{
				PreparedStatement añadir = connection.prepareStatement("INSERT INTO canciones(nombre,tipo,banda,anime,usuario) VALUES(?,?,?,?,?);");
				añadir.setString(1, cancionNombre);
				añadir.setString(2, tipo);
				añadir.setString(3, banda);
				añadir.setString(4, anime);
				nombreUsuario = result.getString("username");
				añadir.setString(5, nombreUsuario);
				añadir.executeUpdate();
				return "redirect:/";
			}else
			{
				return "añadir";
			}
		}else
		{
			return "redirect:/login";
		}
		// Fin de Autentificacion
	}
	
	@PostMapping("/contacto")
	public static String procesarInfo(	@RequestParam String nombre, 
										@RequestParam String comentario, 
										@RequestParam String email,
										Model template) throws SQLException
	{
		
		Connection connection;
		connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		
		template.addAttribute("nombre", nombre);
		template.addAttribute("comentario", comentario);
		template.addAttribute("email", email);
		template.addAttribute("claseContacto","active");
		
		
		if(nombre.equals("") || comentario.equals("") || email.equals(""))
		{
			//Cargar formulario devuelta
			template.addAttribute("mensajeError","No Puede Haber campos Vacios");
			template.addAttribute("error","alert alert-danger small");
			return "contacto";
		}
		else
		{
			PreparedStatement ps = connection.prepareStatement("INSERT INTO mensajes(nombre,email,comentario) VALUES (?,?,?);");
			ps.setString(1, nombre);
			ps.setString(2, email);
			ps.setString(3, comentario);
			
			ps.executeUpdate();
			
			// enviarCorreo(email,"tommy.1997@live.com.ar",comentario,"asunto");
			return "GraciasContacto";
		}
	}
	
	@GetMapping("/prueba")
	public static String paginaPrueba(Model template) throws SQLException
	{
		/*
		Connection connection;
        connection = DriverManager.getConnection(Settings.db_url, Settings.db_user, Settings.db_password);
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM animes ORDER BY visitas DESC LIMIT 8;");
        
		ResultSet resultado = ps.executeQuery();
		ArrayList<Anime> listaHome;
		listaHome = new ArrayList<Anime>();
		
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
		
		*/
		
        return "account";
	}
	
}
