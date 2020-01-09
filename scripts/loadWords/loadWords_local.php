<?php

//https://stackoverflow.com/questions/21360765/how-to-insert-text-file-to-database-table-phpmyadmin
//Ejecutar desde el navegador con XAMPP
//Es necesario el diccionario de palabras 'spanish.txt'
//Para solucionar problemas con el tiempo de ejecución, cambiar el valor de max_execution_time=120'
// en xampp/php/php.ini

echo "<h1>Script para cargar diccionario de palabras en la BD</h1>";
echo "<br>";

//database credentials
$host= "localhost"; 
$user= "root";
$pass= "";
$db="apalabrados_tsw";

//connect to database
$mysqli = new mysqli($host, $user, $pass, $db);

//check connection
if ($mysqli->connect_errno) {
    printf("Falló la conexión: %s\n", $mysqli->connect_error);
    exit();
}

//Open the txt file for reading
$file = fopen("spanish.txt","r"); 

while(! feof($file))
{
    
//Insert every read line from txt to mysql database
$line = fgets($file);

/* Execute query */
if ($mysqli->query("INSERT INTO palabra (texto) VALUES ('$line')") === TRUE) {
    echo $line;
    echo "<br>";
}else{
    echo "Error insertando '$line'";
    echo "<br>";
}
}
fclose($file);

echo "Script completado!!";
echo "<br>";
?>