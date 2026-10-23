IF DB_ID(N'DevGeniusDB') IS NULL CREATE DATABASE [DevGeniusDB];
GO
USE [DevGeniusDB]
GO
/****** Object:  Table [dbo].[Usuarios]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Usuarios](
	[UsuarioID] [int] IDENTITY(1,1) NOT NULL,
	[Nombre] [nvarchar](100) NULL,
	[Apellido] [nvarchar](100) NULL,
	[CorreoElectronico] [nvarchar](255) NOT NULL,
	[NombreUsuario] [nvarchar](100) NOT NULL,
	[PasswordHash] [nvarchar](512) NOT NULL,
	[PasswordSalt] [nvarchar](256) NOT NULL,
	[AceptaTerminos] [bit] NULL,
	[Activo] [bit] NULL,
	[EmailVerificado] [bit] NULL,
 CONSTRAINT [PK_Usuarios] PRIMARY KEY CLUSTERED 
(
	[UsuarioID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Sesiones]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Sesiones](
	[SesionID] [int] IDENTITY(1,1) NOT NULL,
	[UsuarioID] [int] NOT NULL,
	[Token] [nvarchar](512) NOT NULL,
	[IPAddress] [nvarchar](45) NULL,
	[UserAgent] [nvarchar](500) NULL,
	[FechaCreacion] [datetime2](7) NOT NULL,
	[FechaExpiracion] [datetime2](7) NOT NULL,
	[Revocada] [bit] NOT NULL,
	[FechaRevocacion] [datetime2](7) NULL,
 CONSTRAINT [PK_Sesiones] PRIMARY KEY CLUSTERED 
(
	[SesionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_SesionesActivas]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- Vista de sesiones activas (no expiradas, no revocadas)
CREATE   VIEW [dbo].[vw_SesionesActivas] AS
SELECT
    s.SesionID,
    s.UsuarioID,
    u.NombreUsuario,
    u.CorreoElectronico,
    s.IPAddress,
    s.FechaCreacion,
    s.FechaExpiracion
FROM dbo.Sesiones s
INNER JOIN dbo.Usuarios u ON s.UsuarioID = u.UsuarioID
WHERE s.Revocada = 0
  AND s.FechaExpiracion > GETDATE();
GO
/****** Object:  Table [dbo].[AuditoriaUsuarios]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AuditoriaUsuarios](
	[AuditoriaID] [int] IDENTITY(1,1) NOT NULL,
	[UsuarioID] [int] NOT NULL,
	[Accion] [nvarchar](100) NOT NULL,
	[Descripcion] [nvarchar](500) NULL,
	[IPAddress] [nvarchar](45) NULL,
	[FechaAccion] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_AuditoriaUsuarios] PRIMARY KEY CLUSTERED 
(
	[AuditoriaID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Cursos]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Cursos](
	[CursoID] [int] IDENTITY(1,1) NOT NULL,
	[Titulo] [nvarchar](255) NOT NULL,
	[Descripcion] [nvarchar](max) NULL,
	[NivelDificultad] [nvarchar](50) NULL,
	[ImagenPortada] [nvarchar](500) NULL,
	[Idioma] [nvarchar](50) NULL,
	[EstaPublicado] [bit] NULL,
	[FechaCreacion] [datetime] NULL,
	[FechaPublicacion] [datetime] NULL,
	[XPRecompensa] [int] NULL,
	[InstructorID] [int] NULL,
	[Activo] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[CursoID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[IntentosFallidos]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[IntentosFallidos](
	[IntentoID] [int] IDENTITY(1,1) NOT NULL,
	[UsuarioID] [int] NULL,
	[CorreoIntento] [nvarchar](255) NULL,
	[IPAddress] [nvarchar](45) NULL,
	[FechaIntento] [datetime2](7) NOT NULL,
	[Exitoso] [bit] NOT NULL,
 CONSTRAINT [PK_IntentosFallidos] PRIMARY KEY CLUSTERED 
(
	[IntentoID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Lecciones]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Lecciones](
	[LeccionID] [int] IDENTITY(1,1) NOT NULL,
	[CursoID] [int] NULL,
	[Orden] [int] NULL,
	[Titulo] [nvarchar](255) NOT NULL,
	[Tipo] [nvarchar](50) NULL,
	[Contenido] [nvarchar](max) NULL,
	[CodigoEjemplo] [nvarchar](max) NULL,
	[XPLeccion] [int] NULL,
	[FechaCreacion] [datetime] NULL,
	[Preguntas] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
	[LeccionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RecuperacionContrasena]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RecuperacionContrasena](
	[RecuperacionID] [int] IDENTITY(1,1) NOT NULL,
	[UsuarioID] [int] NOT NULL,
	[Token] [nvarchar](512) NOT NULL,
	[FechaCreacion] [datetime2](7) NOT NULL,
	[FechaExpiracion] [datetime2](7) NOT NULL,
	[Usado] [bit] NOT NULL,
	[IPAddress] [nvarchar](45) NULL,
 CONSTRAINT [PK_RecuperacionContrasena] PRIMARY KEY CLUSTERED 
(
	[RecuperacionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Registro]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Registro](
	[RegistroID] [int] IDENTITY(1,1) NOT NULL,
	[NombreUsuario] [nvarchar](100) NOT NULL,
	[Email] [nvarchar](255) NOT NULL,
	[Contrasena] [nvarchar](512) NOT NULL,
	[ConfirmarContrasena] [nvarchar](512) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RegistroID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[Cursos] ON 

INSERT [dbo].[Cursos] ([CursoID], [Titulo], [Descripcion], [NivelDificultad], [ImagenPortada], [Idioma], [EstaPublicado], [FechaCreacion], [FechaPublicacion], [XPRecompensa], [InstructorID], [Activo]) VALUES (1, N'Introducción a JavaScript', N'Aprende los fundamentos de JavaScript, el lenguaje de programación más popular para el desarrollo web.', N'principiante', N'https://example.com/js-intro.jpg', N'Español', 0, CAST(N'2026-04-23T00:00:00.000' AS DateTime), CAST(N'2026-04-23T00:00:00.000' AS DateTime), 150, 1, 0)
INSERT [dbo].[Cursos] ([CursoID], [Titulo], [Descripcion], [NivelDificultad], [ImagenPortada], [Idioma], [EstaPublicado], [FechaCreacion], [FechaPublicacion], [XPRecompensa], [InstructorID], [Activo]) VALUES (2, N'Desarrollo Web con React', N'Crea aplicaciones web modernas utilizando React, una biblioteca de JavaScript para interfaces de usuario.', N'intermedio', N'https://example.com/react-course.jpg', N'Español', 0, CAST(N'2026-04-23T00:00:00.000' AS DateTime), CAST(N'2026-04-23T00:00:00.000' AS DateTime), 200, 2, 0)
INSERT [dbo].[Cursos] ([CursoID], [Titulo], [Descripcion], [NivelDificultad], [ImagenPortada], [Idioma], [EstaPublicado], [FechaCreacion], [FechaPublicacion], [XPRecompensa], [InstructorID], [Activo]) VALUES (3, N'Bases de Datos SQL Server', N'Domina el diseño y gestión de bases de datos relacionales utilizando SQL Server.', N'intermedio', N'https://example.com/sql-server.jpg', N'Español', 0, CAST(N'2026-04-23T00:00:00.000' AS DateTime), CAST(N'2026-05-20T22:46:15.547' AS DateTime), 180, 2, 0)
INSERT [dbo].[Cursos] ([CursoID], [Titulo], [Descripcion], [NivelDificultad], [ImagenPortada], [Idioma], [EstaPublicado], [FechaCreacion], [FechaPublicacion], [XPRecompensa], [InstructorID], [Activo]) VALUES (4, N'HTML — Fundamentos del Web', N'Aprende a construir páginas web desde cero usando HTML5. Domina la estructura, semántica y buenas prácticas del lenguaje de marcado más importante de la web.', N'principiante', N'https://example.com/html-course.jpg', N'Español', 1, CAST(N'2026-04-30T00:00:00.000' AS DateTime), CAST(N'2026-06-02T00:30:08.147' AS DateTime), 120, 1, 1)
INSERT [dbo].[Cursos] ([CursoID], [Titulo], [Descripcion], [NivelDificultad], [ImagenPortada], [Idioma], [EstaPublicado], [FechaCreacion], [FechaPublicacion], [XPRecompensa], [InstructorID], [Activo]) VALUES (5, N'Curso Completo de Java', N'Aprende Java desde cero hasta programación orientada a objetos', N'Intermedio', N'java-portada.jpg', N'Español', 0, CAST(N'2026-05-14T21:00:28.227' AS DateTime), CAST(N'2026-06-02T00:58:47.060' AS DateTime), 100, 1, 0)
SET IDENTITY_INSERT [dbo].[Cursos] OFF
GO
SET IDENTITY_INSERT [dbo].[Lecciones] ON 

INSERT [dbo].[Lecciones] ([LeccionID], [CursoID], [Orden], [Titulo], [Tipo], [Contenido], [CodigoEjemplo], [XPLeccion], [FechaCreacion], [Preguntas]) VALUES (7, 4, 1, N'¿Qué es HTML?', N'texto', N'HTML (HyperText Markup Language) es el lenguaje estándar para crear páginas web. No es un lenguaje de programación — es un **lenguaje de marcado** que define la estructura y el contenido de una página.

Cada página web que visitas está construida sobre HTML. Los navegadores (Chrome, Firefox, Safari) leen el HTML y lo convierten en lo que ves en pantalla.

**¿Cómo funciona?**
HTML usa **etiquetas** (tags) para marcar el contenido. Una etiqueta le dice al navegador qué tipo de contenido es: un título, un párrafo, una imagen, un enlace, etc.

Las etiquetas tienen esta forma:
- Etiqueta de apertura: `<p>`
- Contenido: "Hola mundo"
- Etiqueta de cierre: `</p>`

Algunas etiquetas son "auto-cerrantes" y no necesitan etiqueta de cierre, como `<img>` o `<br>`.', N'<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8">
    <title>Mi primera página</title>
  </head>
  <body>
    <h1>¡Hola Mundo!</h1>
    <p>Esta es mi primera página web.</p>
  </body>
</html>', 20, CAST(N'2026-05-12T21:02:32.173' AS DateTime), NULL)
INSERT [dbo].[Lecciones] ([LeccionID], [CursoID], [Orden], [Titulo], [Tipo], [Contenido], [CodigoEjemplo], [XPLeccion], [FechaCreacion], [Preguntas]) VALUES (8, 4, 2, N'Estructura básica de un documento HTML', N'texto', N'Todo documento HTML válido tiene una estructura fija que debes conocer de memoria.

**`<!DOCTYPE html>`**
Declara que el documento usa HTML5. Siempre va en la primera línea. Le dice al navegador qué versión de HTML usar.

**`<html lang="es">`**
El elemento raíz que contiene todo el documento. El atributo `lang` indica el idioma del contenido, lo cual es importante para accesibilidad y SEO.

**`<head>`**
Contiene metadatos (información sobre la página que no se muestra directamente):
- `<meta charset="UTF-8">` — codificación de caracteres (permite tildes, ñ, etc.)
- `<meta name="viewport">` — esencial para diseño responsivo
- `<title>` — el texto que aparece en la pestaña del navegador
- `<link>` — para conectar hojas de estilo CSS

**`<body>`**
Todo lo que va aquí **sí se muestra** en el navegador: textos, imágenes, botones, formularios, etc.', N'<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Mi sitio web personal">
    <title>Mi Sitio Web</title>
    <link rel="stylesheet" href="estilos.css">
  </head>
  <body>
    <!-- Todo el contenido visible va aquí -->
    <h1>Bienvenido</h1>
    <p>Este es el cuerpo de mi página.</p>
  </body>
</html>', 20, CAST(N'2026-05-12T21:02:32.180' AS DateTime), NULL)
INSERT [dbo].[Lecciones] ([LeccionID], [CursoID], [Orden], [Titulo], [Tipo], [Contenido], [CodigoEjemplo], [XPLeccion], [FechaCreacion], [Preguntas]) VALUES (9, 4, 3, N'Encabezados y párrafos', N'texto', N'**Encabezados (Headings)**
HTML tiene 6 niveles de encabezados, de `<h1>` a `<h6>`. `<h1>` es el más importante (y el más grande visualmente), `<h6>` el menos importante.

Reglas importantes:
- Solo debe haber **un `<h1>` por página** — es el título principal
- Los encabezados deben usarse en orden jerárquico (no saltar de `<h1>` a `<h4>`)
- Los encabezados no son solo para hacer texto grande — tienen significado semántico

**Párrafos**
La etiqueta `<p>` define un párrafo de texto. El navegador automáticamente añade espacio antes y después de cada párrafo.

**Saltos de línea y separadores**
- `<br>` — salto de línea dentro de un párrafo (auto-cerrante)
- `<hr>` — línea horizontal separadora (auto-cerrante)

**Formato de texto**
- `<strong>` — texto en **negrita** (importancia semántica)
- `<em>` — texto en *cursiva* (énfasis semántico)
- `<mark>` — texto resaltado
- `<small>` — texto más pequeño', N'<h1>Título principal de la página</h1>
<h2>Sección: Sobre mí</h2>
<h3>Subsección: Mi experiencia</h3>

<p>
  Soy un desarrollador web con <strong>5 años de experiencia</strong>
  en <em>tecnologías frontend</em>.
</p>

<p>
  Me especializo en HTML, CSS y JavaScript.<br>
  También trabajo con React y Vue.js.
</p>

<hr>

<h2>Sección: Mis proyectos</h2>
<p>
  Aquí encontrarás <mark>mis mejores proyectos</mark>.
  <small>(Actualizado en 2026)</small>
</p>', 25, CAST(N'2026-05-12T21:02:32.183' AS DateTime), NULL)
INSERT [dbo].[Lecciones] ([LeccionID], [CursoID], [Orden], [Titulo], [Tipo], [Contenido], [CodigoEjemplo], [XPLeccion], [FechaCreacion], [Preguntas]) VALUES (10, 4, 4, N'Listas: ordenadas y desordenadas', N'texto', N'Las listas son esenciales para organizar información en HTML. Existen tres tipos:

**Lista desordenada `<ul>`** (Unordered List)
Muestra ítems con viñetas (•). Úsala cuando el orden no importa, como una lista de ingredientes o características.

**Lista ordenada `<ol>`** (Ordered List)
Muestra ítems numerados (1, 2, 3...). Úsala cuando el orden sí importa, como pasos de un proceso.

**Lista de descripción `<dl>`** (Description List)
Para pares de término-definición. Usa `<dt>` para el término y `<dd>` para la descripción.

**Listas anidadas**
Puedes anidar listas dentro de otras listas para crear jerarquías.', N'<ul>
  <li>Pan</li>
  <li>Leche</li>
  <li>Huevos</li>
</ul>

<ol>
  <li>Precalentar el horno</li>
  <li>Mezclar los ingredientes</li>
  <li>Hornear por 30 minutos</li>
</ol>

<dl>
  <dt>HTML</dt>
  <dd>Lenguaje de marcado para web</dd>
  <dt>CSS</dt>
  <dd>Lenguaje de estilos para diseño web</dd>
</dl>', 25, CAST(N'2026-05-12T21:02:32.190' AS DateTime), NULL)
INSERT [dbo].[Lecciones] ([LeccionID], [CursoID], [Orden], [Titulo], [Tipo], [Contenido], [CodigoEjemplo], [XPLeccion], [FechaCreacion], [Preguntas]) VALUES (11, 4, 5, N'Imágenes y enlaces', N'texto', N'**Imágenes con `<img>`**
La etiqueta `<img>` inserta una imagen. Es auto-cerrante y tiene atributos clave:
- `src` — ruta o URL de la imagen **(obligatorio)**
- `alt` — texto alternativo **(obligatorio)** — vital para accesibilidad
- `width` y `height` — dimensiones en píxeles
- `loading="lazy"` — carga la imagen solo cuando está a punto de ser visible (mejora rendimiento)

**Enlaces con `<a>`**
La etiqueta `<a>` (anchor) crea hipervínculos. El atributo `href` define el destino.
- `target="_blank"` — abre en pestaña nueva
- `href="mailto:x@x.com"` — enlace para enviar email
- `href="tel:+123456"` — enlace para llamar (móviles)
- `href="#seccion"` — enlace a un ID dentro de la misma página', N'<img src="./imagenes/perfil.jpg" alt="Foto de perfil" width="200" height="200">

<a href="https://github.com" target="_blank">Mi GitHub</a>
<a href="mailto:contacto@ejemplo.com">Envíame un email</a>

<section id="proyectos">
  <h2>Mis Proyectos</h2>
</section>', 30, CAST(N'2026-05-12T21:02:32.197' AS DateTime), NULL)
INSERT [dbo].[Lecciones] ([LeccionID], [CursoID], [Orden], [Titulo], [Tipo], [Contenido], [CodigoEjemplo], [XPLeccion], [FechaCreacion], [Preguntas]) VALUES (12, 4, 6, N'Quiz: Demuestra tu conocimiento', N'quiz', N'Responde correctamente estas preguntas para demostrar que has aprendido los conceptos de HTML.', N'', 100, CAST(N'2026-05-12T21:02:32.203' AS DateTime), N'[{"pregunta":"¿Qué significa HTML?","opciones":["HyperText Markup Language","High Tech Modern Language","Home Tool Markup Language","Hyper Transfer Markup Language"],"respuestaCorrecta":0,"explicacion":"HTML significa HyperText Markup Language, el lenguaje estándar para crear páginas web."},{"pregunta":"¿Cuál es la estructura básica de un documento HTML?","opciones":["<html><head><body>","<head><body><html>","<body><head><html>","<html><body><head>"],"respuestaCorrecta":0,"explicacion":"La estructura básica es <!DOCTYPE html><html><head>...</head><body>...</body></html>"},{"pregunta":"¿Qué etiqueta se usa para crear un párrafo?","opciones":["<p>","<para>","<paragraph>","<text>"],"respuestaCorrecta":0,"explicacion":"La etiqueta <p> se usa para definir párrafos de texto."},{"pregunta":"¿Qué hace la etiqueta <!DOCTYPE html>?","opciones":["Define el título de la página","Declara que usa HTML5","Enlaza el CSS","Define el idioma"],"respuestaCorrecta":1,"explicacion":"La declaración <!DOCTYPE html> le dice al navegador que el documento usa HTML5."},{"pregunta":"¿Dónde va el contenido visible de la página?","opciones":["En <head>","En <html>","En <body>","En <title>"],"respuestaCorrecta":2,"explicacion":"Todo el contenido visible va dentro de la etiqueta <body>."},{"pregunta":"¿Cuántos niveles de encabezados tiene HTML?","opciones":["3","5","6","8"],"respuestaCorrecta":2,"explicacion":"HTML tiene 6 niveles de encabezados: h1, h2, h3, h4, h5 y h6."},{"pregunta":"¿Cuál es la regla más importante sobre encabezados?","opciones":["Deben ser coloridos","Solo uno por página","Deben estar en mayúsculas","Solo para títulos"],"respuestaCorrecta":1,"explicacion":"Solo debe haber un <h1> por página, ya que representa el título principal."},{"pregunta":"¿Qué tipo de lista muestra viñetas?","opciones":["<ol>","<ul>","<dl>","<list>"],"respuestaCorrecta":1,"explicacion":"La lista desordenada <ul> muestra ítems con viñetas (•)."},{"pregunta":"¿Qué tipo de lista muestra números?","opciones":["<ul>","<ol>","<dl>","<nl>"],"respuestaCorrecta":1,"explicacion":"La lista ordenada <ol> muestra ítems numerados (1, 2, 3...)."},{"pregunta":"¿Qué etiqueta se usa para crear un enlace?","opciones":["<link>","<a>","<url>","<href>"],"respuestaCorrecta":1,"explicacion":"La etiqueta <a> (anchor) se usa para crear enlaces a otras páginas."}]')
SET IDENTITY_INSERT [dbo].[Lecciones] OFF
GO
SET IDENTITY_INSERT [dbo].[Usuarios] ON 

INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (3, N'David', N'suarez', N'david@example.com', N'david.suarez', N'$2b$10$zMNnQtP6KJkIDVlrjn2yiuFO.rnnmEo2wHlAQx5XSeT8Kz4uYNHTO', N'$2b$10$zMNnQtP6KJkIDVlrjn2yiu', 0, 1, 0)
INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (4, N'Daniela', N'Suarez', N'daniela@gmail.com', N'daniela.suarez', N'$2b$10$wAxYjidpmjlmtXwI7TjMMubQzrvu6dZtCj4I/cQC4AD/oQZrP7nbO', N'$2b$10$wAxYjidpmjlmtXwI7TjMMu', 0, 1, 0)
INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (5, N'diego', N'delgado', N'diego@gmail.com', N'diego.delgado', N'$2b$10$vVilWwCfIW1OGiltbOiEH.bcQOOFLvx6lTVR9p.72dusUCsKJT/K6', N'$2b$10$vVilWwCfIW1OGiltbOiEH.', 0, 1, 0)
INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (6, N'Administrador', N'Sistema', N'admin@admin.com', N'admin', N'$2b$10$QDJ3p0Zhhgn86VpUZb.k7uz3Fn.7Crhnkl7rzlys9GDdTf.DHO0Eu', N'$2b$10$QDJ3p0Zhhgn86VpUZb.k7u', 0, 1, 1)
INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (11, N'Juan', N'Osorio', N'juan.osorio@gmail.com', N'juan.osorio', N'$2b$10$E.XF0.IM1I0JBwzB6utH/uHd4pokA/QRBKlDKFUhea6y9ha/qO1x6', N'$2b$10$E.XF0.IM1I0JBwzB6utH/u', 0, 1, 0)
INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (12, N'Carlos', N'Caro', N'carlos123@gmail.com', N'Cachon', N'$2b$10$g8yjc9ilpjwHl1S5G3zgD.ksTGeHLjHmtVuu.Op07vHaCYiA5KVt6', N'$2b$10$g8yjc9ilpjwHl1S5G3zgD.', 0, 1, 1)
INSERT [dbo].[Usuarios] ([UsuarioID], [Nombre], [Apellido], [CorreoElectronico], [NombreUsuario], [PasswordHash], [PasswordSalt], [AceptaTerminos], [Activo], [EmailVerificado]) VALUES (13, N'maicol', N'franco', N'maicol@gmail.com', N'maicol.franco', N'$2b$10$lXoqJjbc/AiB8RnNtdYIhuyCuJlnJJpTjNoqFS8gGthojgy3Wi36e', N'$2b$10$lXoqJjbc/AiB8RnNtdYIhu', 0, 1, 0)
SET IDENTITY_INSERT [dbo].[Usuarios] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_IntentosFallidos_IP_Fecha]    Script Date: 2/06/2026 10:49:13 a. m. ******/
CREATE NONCLUSTERED INDEX [IX_IntentosFallidos_IP_Fecha] ON [dbo].[IntentosFallidos]
(
	[IPAddress] ASC,
	[FechaIntento] ASC
)
WHERE ([Exitoso]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Recuperacion_Token]    Script Date: 2/06/2026 10:49:13 a. m. ******/
CREATE NONCLUSTERED INDEX [IX_Recuperacion_Token] ON [dbo].[RecuperacionContrasena]
(
	[Token] ASC
)
WHERE ([Usado]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Sesiones_Token]    Script Date: 2/06/2026 10:49:13 a. m. ******/
CREATE NONCLUSTERED INDEX [IX_Sesiones_Token] ON [dbo].[Sesiones]
(
	[Token] ASC
)
WHERE ([Revocada]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Sesiones_UsuarioID]    Script Date: 2/06/2026 10:49:13 a. m. ******/
CREATE NONCLUSTERED INDEX [IX_Sesiones_UsuarioID] ON [dbo].[Sesiones]
(
	[UsuarioID] ASC,
	[Revocada] ASC,
	[FechaExpiracion] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_Usuarios_Correo]    Script Date: 2/06/2026 10:49:13 a. m. ******/
ALTER TABLE [dbo].[Usuarios] ADD  CONSTRAINT [UQ_Usuarios_Correo] UNIQUE NONCLUSTERED 
(
	[CorreoElectronico] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_Usuarios_NombreUsuario]    Script Date: 2/06/2026 10:49:13 a. m. ******/
ALTER TABLE [dbo].[Usuarios] ADD  CONSTRAINT [UQ_Usuarios_NombreUsuario] UNIQUE NONCLUSTERED 
(
	[NombreUsuario] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Usuarios_Correo]    Script Date: 2/06/2026 10:49:13 a. m. ******/
CREATE NONCLUSTERED INDEX [IX_Usuarios_Correo] ON [dbo].[Usuarios]
(
	[CorreoElectronico] ASC
)
INCLUDE([PasswordHash],[PasswordSalt],[Activo],[EmailVerificado]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Usuarios_NombreUsuario]    Script Date: 2/06/2026 10:49:13 a. m. ******/
CREATE NONCLUSTERED INDEX [IX_Usuarios_NombreUsuario] ON [dbo].[Usuarios]
(
	[NombreUsuario] ASC
)
INCLUDE([PasswordHash],[PasswordSalt],[Activo]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[AuditoriaUsuarios] ADD  DEFAULT (getdate()) FOR [FechaAccion]
GO
ALTER TABLE [dbo].[Cursos] ADD  DEFAULT ((1)) FOR [EstaPublicado]
GO
ALTER TABLE [dbo].[Cursos] ADD  DEFAULT (getdate()) FOR [FechaCreacion]
GO
ALTER TABLE [dbo].[Cursos] ADD  DEFAULT ((0)) FOR [Activo]
GO
ALTER TABLE [dbo].[IntentosFallidos] ADD  DEFAULT (getdate()) FOR [FechaIntento]
GO
ALTER TABLE [dbo].[IntentosFallidos] ADD  DEFAULT ((0)) FOR [Exitoso]
GO
ALTER TABLE [dbo].[Lecciones] ADD  DEFAULT (getdate()) FOR [FechaCreacion]
GO
ALTER TABLE [dbo].[RecuperacionContrasena] ADD  DEFAULT (getdate()) FOR [FechaCreacion]
GO
ALTER TABLE [dbo].[RecuperacionContrasena] ADD  DEFAULT (dateadd(hour,(2),getdate())) FOR [FechaExpiracion]
GO
ALTER TABLE [dbo].[RecuperacionContrasena] ADD  DEFAULT ((0)) FOR [Usado]
GO
ALTER TABLE [dbo].[Sesiones] ADD  DEFAULT (getdate()) FOR [FechaCreacion]
GO
ALTER TABLE [dbo].[Sesiones] ADD  DEFAULT ((0)) FOR [Revocada]
GO
ALTER TABLE [dbo].[Usuarios] ADD  DEFAULT ((0)) FOR [AceptaTerminos]
GO
ALTER TABLE [dbo].[Usuarios] ADD  DEFAULT ((1)) FOR [Activo]
GO
ALTER TABLE [dbo].[Usuarios] ADD  DEFAULT ((0)) FOR [EmailVerificado]
GO
ALTER TABLE [dbo].[AuditoriaUsuarios]  WITH CHECK ADD  CONSTRAINT [FK_Auditoria_Usuarios] FOREIGN KEY([UsuarioID])
REFERENCES [dbo].[Usuarios] ([UsuarioID])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[AuditoriaUsuarios] CHECK CONSTRAINT [FK_Auditoria_Usuarios]
GO
ALTER TABLE [dbo].[IntentosFallidos]  WITH CHECK ADD  CONSTRAINT [FK_Intentos_Usuarios] FOREIGN KEY([UsuarioID])
REFERENCES [dbo].[Usuarios] ([UsuarioID])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[IntentosFallidos] CHECK CONSTRAINT [FK_Intentos_Usuarios]
GO
ALTER TABLE [dbo].[Lecciones]  WITH CHECK ADD FOREIGN KEY([CursoID])
REFERENCES [dbo].[Cursos] ([CursoID])
GO
ALTER TABLE [dbo].[RecuperacionContrasena]  WITH CHECK ADD  CONSTRAINT [FK_Recuperacion_Usuarios] FOREIGN KEY([UsuarioID])
REFERENCES [dbo].[Usuarios] ([UsuarioID])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[RecuperacionContrasena] CHECK CONSTRAINT [FK_Recuperacion_Usuarios]
GO
ALTER TABLE [dbo].[Sesiones]  WITH CHECK ADD  CONSTRAINT [FK_Sesiones_Usuarios] FOREIGN KEY([UsuarioID])
REFERENCES [dbo].[Usuarios] ([UsuarioID])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Sesiones] CHECK CONSTRAINT [FK_Sesiones_Usuarios]
GO
ALTER TABLE [dbo].[Usuarios]  WITH CHECK ADD  CONSTRAINT [CK_Usuarios_Apellido] CHECK  ((len(ltrim(rtrim([Apellido])))>=(2)))
GO
ALTER TABLE [dbo].[Usuarios] CHECK CONSTRAINT [CK_Usuarios_Apellido]
GO
ALTER TABLE [dbo].[Usuarios]  WITH CHECK ADD  CONSTRAINT [CK_Usuarios_Correo] CHECK  (([CorreoElectronico] like '%_@_%._%'))
GO
ALTER TABLE [dbo].[Usuarios] CHECK CONSTRAINT [CK_Usuarios_Correo]
GO
ALTER TABLE [dbo].[Usuarios]  WITH CHECK ADD  CONSTRAINT [CK_Usuarios_Nombre] CHECK  ((len(ltrim(rtrim([Nombre])))>=(2)))
GO
ALTER TABLE [dbo].[Usuarios] CHECK CONSTRAINT [CK_Usuarios_Nombre]
GO
ALTER TABLE [dbo].[Usuarios]  WITH CHECK ADD  CONSTRAINT [CK_Usuarios_NombreUsuario] CHECK  ((len([NombreUsuario])>=(4)))
GO
ALTER TABLE [dbo].[Usuarios] CHECK CONSTRAINT [CK_Usuarios_NombreUsuario]
GO
/****** Object:  StoredProcedure [dbo].[sp_LoginUsuario]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_LoginUsuario]
    @Identificador  NVARCHAR(255),   -- Correo o nombre de usuario
    @IPAddress      NVARCHAR(45)  = NULL,
    @UserAgent      NVARCHAR(500) = NULL,
    @Resultado      NVARCHAR(100) OUTPUT,
    @UsuarioID      INT           OUTPUT,
    @PasswordHash   NVARCHAR(512) OUTPUT,
    @PasswordSalt   NVARCHAR(256) OUTPUT,
    @NombreCompleto NVARCHAR(210) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY

        -- Verificar bloqueo por intentos (más de 5 en los últimos 15 minutos desde esta IP)
        DECLARE @IntentosRecientes INT;
        SELECT @IntentosRecientes = COUNT(*)
        FROM dbo.IntentosFallidos
        WHERE IPAddress = @IPAddress
          AND Exitoso = 0
          AND FechaIntento >= DATEADD(MINUTE, -15, GETDATE());

        IF @IntentosRecientes >= 5
        BEGIN
            SET @Resultado = 'ERROR_BLOQUEADO_IP';
            SET @UsuarioID = -1;
            RETURN;
        END

        -- Buscar usuario por correo o nombre de usuario
        SELECT
            @UsuarioID      = UsuarioID,
            @PasswordHash   = PasswordHash,
            @PasswordSalt   = PasswordSalt,
            @NombreCompleto = Nombre + ' ' + Apellido
        FROM dbo.Usuarios
        WHERE (CorreoElectronico = LOWER(LTRIM(RTRIM(@Identificador)))
               OR NombreUsuario  = LOWER(LTRIM(RTRIM(@Identificador))))
          AND Activo = 1;

        IF @UsuarioID IS NULL
        BEGIN
            -- Registrar intento fallido (usuario no existe)
            INSERT INTO dbo.IntentosFallidos (CorreoIntento, IPAddress, Exitoso)
            VALUES (@Identificador, @IPAddress, 0);

            SET @Resultado = 'ERROR_CREDENCIALES';
            SET @UsuarioID = -1;
            RETURN;
        END

        -- El hash se verifica en la capa de aplicación.
        -- Si el hash es válido, el backend llama a sp_RegistrarSesion.
        -- Aquí retornamos los datos necesarios para verificar.
        SET @Resultado = 'PENDIENTE_VERIFICACION';

    END TRY
    BEGIN CATCH
        SET @Resultado = 'ERROR_INTERNO: ' + ERROR_MESSAGE();
        SET @UsuarioID = -1;
    END CATCH
END
GO
/****** Object:  StoredProcedure [dbo].[sp_SolicitarRecuperacion]    Script Date: 2/06/2026 10:49:13 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_SolicitarRecuperacion]
    @CorreoElectronico  NVARCHAR(255),
    @Token              NVARCHAR(512),
    @IPAddress          NVARCHAR(45)  = NULL,
    @Resultado          NVARCHAR(100) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @UID INT;

    SELECT @UID = UsuarioID
    FROM dbo.Usuarios
    WHERE CorreoElectronico = LOWER(LTRIM(RTRIM(@CorreoElectronico)))
      AND Activo = 1;

    -- Se responde siempre OK por seguridad (no revelar si el correo existe)
    IF @UID IS NOT NULL
    BEGIN
        -- Invalidar tokens anteriores
        UPDATE dbo.RecuperacionContrasena
        SET Usado = 1
        WHERE UsuarioID = @UID AND Usado = 0;

        INSERT INTO dbo.RecuperacionContrasena (UsuarioID, Token, IPAddress)
        VALUES (@UID, @Token, @IPAddress);

        INSERT INTO dbo.AuditoriaUsuarios (UsuarioID, Accion, IPAddress)
        VALUES (@UID, 'SOLICITUD_RECUPERACION', @IPAddress);
    END

    SET @Resultado = 'OK';
END
GO