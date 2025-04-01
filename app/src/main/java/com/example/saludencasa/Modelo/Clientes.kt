package com.example.saludencasa.Modelo

data class Clientes(
    val id_cliente: Int,
    val cedula: String,
    val nombre: String,
    val apellido: String,
    val fecha_nacimiento: String,
    val sexo: Int,
    val telefono: String,
    val pais: Int,
    val provincia: Int,
    val ciudad: Int,
    val referencia_de_domicilio: String,
    val tipo_sangre: Int,
    val sexodescrip: String,
    val paisdescrip: String,
    val provinciadescrip: String,
    val ciudaddescrip: String,
    val sangredescrip: String,
    val foto: String
)

data class ClientesPUT(
    val id_cliente: Int,
    val cedula: String,
    val nombre: String,
    val apellido: String,
    val fecha_nacimiento: String,
    val sexo: Int,
    val telefono: String,
    val pais: Int,
    val provincia: Int,
    val ciudad: Int,
    val referencia_de_domicilio: String,
    val tipo_sangre: Int
    //,val foto: String
)