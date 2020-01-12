# Apalabretior
>Trabajo de prácticas de la asignatura __Tecnologías y Sistemas Web__. Implementación de una aplicación web (cliente-servidor) que ofrece a los usuarios la posibilidad de jugar a un juego tipo Apalabrados, un juego de tablero de dos contra dos que consiste en formar palabras cruzadas.


## Tecnologías utilizadas 🛠

- HTML5, CSS3, Javascript
- Bootstrap 4
- JQuery, con las librerías jqueryui y jqueryui-touch-punch.
- KnockoutJS
- Spring
- MySQL
- Selenium y JUnit para pruebas funcionales.
- JMeter para pruebas de rendimiento

---

## Funcionalidades implementadas 📋

### Gestión de cuentas

#### Identificación y registro mediante redes sociales
Hemos incluido el inicio de sesión mediante Google utilizando el protocolo OAuth.

<p align="center">
<img src="/screenshot/cuentas/inicioConGoogle.PNG" width="700" > 
</p>

#### Recuperación de contraseña
La recuperación de contraseña la hemos implementado mediante el envío al correo electrónico del usuario un link con un token con fecha de caducidad.

<p align="center">
<img src="/screenshot/cuentas/recuperarPwd1.PNG" width="700" > 

<p align="center">
<img src="/screenshot/cuentas/recuperarPwd2.PNG" width="700">
</p>

#### Asociación de una imagen al usuario
Cuando un usuario se registra en el sistema, se le asigna una imagen por defecto. Sin embargo, hemos implementado un caso de uso para que el usuario, una vez logueado, pueda actualizar su imagen de perfil. 

<p align="center">
<img src="/screenshot/cuentas/cambioImagenUsuario.PNG" width="700">
</p>

### Partidas

#### Solicitar partida
En una partida normal, cada jugador recibe siete fichas y el servidor asigna el turno aleatoriamente a a uno de los dos. El jugador que tiene el turno, debe formar alguna palabra con las letras que disponga. 

<p align="center">
<img src="/screenshot/partidas/solicitarPartida.PNG" width="700">
</p>

#### Enviar movimiento
Cuando el jugador ha colocado las letras en el tablero, pulsa el botón "JUGAR" y el servidor valida si todas las palabras que pueden formarse son correctas. Si alguna no es correcta, se muestra al usuario un mensaje de error, como se muestra en la figura siguiente:

<p align="center">
<img src="/screenshot/partidas/movimientoError.PNG" width="700">
</p>

Si todas las palabras son correctas, el servidor informa al usuario de la puntuación que conseguirá y le solicita confirmación de la jugada. Si confirma, el servidor le da nuevas letras hasta completar siete, y el turno pasa al siguiente jugador. 

<p align="center">
<img src="/screenshot/partidas/movimientoOK.PNG" width="700">
</p>

#### Cambio de letras
El jugador que tiene el turno puede solicitar al servidor que le dé nuevas letras. Para ello, selecciona qué letras, de entre las que dispone, quiere cambiar. En la siguiente figura, el jugador ha elegido cambiar las letras C, D y E. Si confirma, el servidor le dará otras tres letras. Si no confirma, la partida sigue en el estado que se encontraba. 

<p align="center">
<img src="/screenshot/partidas/cambioLetras.PNG" width="700">
</p>

#### Paso de turno
Otra de las posibilidades que ofrece el juego es pasar el turno sin realizar ningún movimiento. Para ello, el usuario ha de pulsar el botón “pasar” del menú de opciones.

#### Mezclar
Si el jugador desea que su cliente le reordene aleatoriamente las letras que tiene en su panel. 

#### Llamar
El usuario puede pulsar el botón "Llamar" y las letras que ha colocado en el tablero vuelven de nuevo al panel. 

#### Abandono
Por último, un jugador puede rendirse en cualquier momento y la partida se le da por perdida. 

<p align="center">
<img src="/screenshot/partidas/perdedor.PNG" width="700">
</p>

---
## Ejecutando las pruebas ⚙️


---
## Autores ✒️

* **Sergio González Velázquez** - [sergiogonzalezvelazquez](https://github.com/SergioGonzalezVelazquez)
* **Francisco de la Mata Rodríguez** - [xylons](https://github.com/Xylons)


