# 3. Resultados por clase de prueba

Todos los tests se ejecutan con JUnit 5 + Mockito. Los servicios y controladores se prueban
aislados: se mockean los repositorios (`@Mock`) y se inyectan en la clase bajo prueba (`@InjectMocks`).
En los controladores se usa un `ExtendedModelMap` real para inspeccionar los atributos que se
cargan al modelo y un `RedirectAttributesModelMap` para verificar los mensajes de redirección.

---

## ObjetivoTest — HU: Objetivo del socio

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| opciones_contieneLosCuatroObjetivosDelCatalogo | Que el catálogo expone los 4 objetivos válidos y que `esValido` los reconoce. | OK |
| etiquetaDe_sinObjetivo_devuelveSinDefinir | Que ante `null`, texto en blanco o código desconocido la etiqueta sea "Sin definir". | OK |
| socioSinObjetivo_muestraSinDefinir | Que un socio recién creado, sin objetivo, muestre "Sin definir" sin fallar. | OK |
| socioConObjetivo_muestraLaEtiquetaDelObjetivo | Que un socio con objetivo muestre la etiqueta correspondiente ("Bajar de peso"). | OK |

---

## SocioServiceTest — HU: Objetivo del socio

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| actualizarObjetivo_seleccionaUno_loGuardaEnElSocio | Que al elegir un objetivo del catálogo, quede guardado en el socio. | OK |
| actualizarObjetivo_cambiaElValorVigente | Que cambiar el objetivo sobrescriba el valor anterior, sin acumularlo ni ignorarlo. | OK |
| actualizarObjetivo_valorVacio_dejaElObjetivoEnNull | Que la opción "Sin definir" deje la columna del objetivo en `null`. | OK |
| actualizarObjetivo_codigoInvalido_lanzaErrorYNoGuarda | Que un código fuera del catálogo se rechace con mensaje claro y sin tocar el valor vigente. | OK |

---

## SocioControllerTest — HU: Contador de socios / Mensaje sin resultados / Objetivo

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| listar_calculaCorrectamenteLosContadores | Que los contadores de activos, inactivos y vencidos reflejen correctamente la lista filtrada. | OK |
| listar_sinResultados_marcaHayFiltrosAplicadosSiHayTexto | Que al buscar un texto sin coincidencias, se marque hayFiltrosAplicados = true. | OK |
| listar_sinSociosYSinFiltros_noMarcaHayFiltrosAplicados | Que sin filtros aplicados y sin socios cargados, no se marque hayFiltrosAplicados. | OK |
| guardarObjetivo_seleccionaUno_guardaYCuelveAlPerfil | Que al elegir un objetivo se guarde, se redirija a la ficha del socio y se muestre el mensaje de éxito. | OK |
| guardarObjetivo_codigoInvalido_muestraErrorYNoGuarda | Que un objetivo inválido vuelva al perfil con el error en flash y sin guardar nada. | OK |
| guardarObjetivo_opcionSinDefinir_pasaElValorVacio | Que la opción "Sin definir" se envíe como valor vacío y rediriga al perfil. | OK |
| mostrarFormularios_incluyenElCatalogoDeObjetivos | Que el formulario de alta y el de edición manden el catálogo de objetivos al modelo. | OK |

---

## CuotaServiceTest — HU: Configurar el valor de la cuota

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| obtenerValorVigente_devuelveElValorMasReciente | Que el Service devuelva correctamente el último valor de cuota cargado. | OK |
| actualizarValorCuota_creaUnRegistroNuevoConFechaDeHoy | Que al cambiar el valor de la cuota se cree un registro nuevo (no se pise el anterior), con la fecha de hoy. | OK |

---

## CuotaControllerTest — HU: Fecha de pago precargada

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| verPagos_incluyeLaFechaDeHoyEnElModelo | Que el perfil del socio reciba la fecha de hoy para precargar el campo de pago. | OK |

---

## EntrenoServiceTest — HU: Registro de entrenamientos

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| registrar_asignaElEjercicioAlSocioConLaFechaCargada | Que el ejercicio quede asociado al socio, con la fecha, series, repeticiones y peso indicados. | OK |
| registrar_sinFecha_usaLaFechaDeHoy | Que al no informar la fecha, se grabe la fecha de hoy. | OK |
| registrar_variosEjerciciosElMismoDia_guardaUnoPorEjercicio | Que cargar varios ejercicios el mismo día genere un registro por ejercicio, todos bajo esa fecha. | OK |
| registrar_conSeriesNoPositivas_lanzaError | Que las series en cero o negativas se rechacen con mensaje claro y sin guardar. | OK |
| registrar_conRepeticionesNoPositivas_lanzaError | Que las repeticiones en cero o negativas se rechacen con mensaje claro y sin guardar. | OK |
| registrar_sinEjercicio_lanzaError | Que un nombre de ejercicio en blanco se rechace. | OK |
| registrar_conPesoNegativo_lanzaError | Que un peso negativo se rechace con mensaje claro. | OK |
| eliminar_borraElEntrenamientoDelSocio | Que al eliminar, se borre el registro que pertenece al socio. | OK |
| eliminar_entrenamientoDeOtroSocio_lanzaError | Que no se pueda borrar un entrenamiento ajeno al socio. | OK |
| listarPorSocio_devuelveElHistorialDelSocio | Que el historial del socio se obtenga del repositorio en el orden esperado. | OK |

---

## EntrenoControllerTest — HU: Entrenamientos del socio

| Método de prueba | Qué verifica | Resultado |
|---|---|---|
| verEntrenamientos_cargaElHistorialDelSocio | Que se carguen el socio, su historial de entrenamientos y la fecha de hoy en el modelo, con la vista correcta. | OK |
| guardar_registraElEjercicioYRedirige | Que se registre el ejercicio con todos sus datos y se redirija al historial con mensaje de éxito. | OK |
| guardar_conSeriesEnCero_muestraElError | Que una validación inválida no rompa la navegación: se redirige y se informa el error. | OK |
| eliminar_borraElRegistroYRedirige | Que se elimine el registro y se vuelva al historial con mensaje de éxito. | OK |
| eliminar_conError_muestraElMensajeEnFlash | Que si el borrado falla, se informe el motivo sin romper la navegación. | OK |

---

## Resumen

| Clase de prueba | Tests | Resultado |
|---|---|---|
| ObjetivoTest | 4 | OK |
| SocioServiceTest | 4 | OK |
| SocioControllerTest | 7 | OK |
| CuotaServiceTest | 2 | OK |
| CuotaControllerTest | 1 | OK |
| EntrenoServiceTest | 10 | OK |
| EntrenoControllerTest | 5 | OK |
| **Total** | **33** | **OK** |