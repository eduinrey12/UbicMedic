package com.example.saludencasa

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.saludencasa.ApiServices.ChatsService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Chats
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.content.FileProvider
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.MensajesService
import com.example.saludencasa.Modelo.Citas
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

class Reportes : AppCompatActivity() {

    private lateinit var spnChats: Spinner
    private lateinit var spnClientesCita: Spinner
    private lateinit var spnEstadoCita: Spinner
    private lateinit var apiChats: ChatsService
    private lateinit var apiMensaje:MensajesService
    private lateinit var apiCita:CitasService

    //DIALOGS
    private lateinit var ckFecha : CheckBox
    private lateinit var ckCliente : CheckBox
    private lateinit var cktodosChats : CheckBox
    private lateinit var ckestadoCita: CheckBox
    private lateinit var txtFechaIni : EditText

    var idChat: Int=0
    var idMensaje: Int=0
    var idCitasg: Int=0
    var idTrabajador: Int=0
    var idCliente: Int=0
    var selectedOption: String=""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        idCliente = intent.extras!!.getInt("IDCLIENTE")
        idTrabajador = intent.extras!!.getInt("IDTRABAJADOR")

        val btnChats : Button = findViewById(R.id.btnReporteChat)
        val btnCita : Button = findViewById(R.id.btnReporteCita)


        btnChats.setOnClickListener {
            showDialogReportes(idCliente,idTrabajador)
        }

        btnCita.setOnClickListener {
            showDialogReportesCita()
        }

        val generateReportButtonCli = findViewById<Button>(R.id.btnReporteClientes)
        generateReportButtonCli.setOnClickListener {
            generatePDFReportCli()
        }

    }

    //DIALOG CHATS
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogReportes(idCliente: Int, idTrabajador: Int) {
        val dialogReportes = Dialog(this)
        dialogReportes.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogReportes.setCancelable(true)
        dialogReportes.setContentView(R.layout.dialog_reportes)
        dialogReportes.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtEncabezado : TextView = dialogReportes.findViewById(R.id.txtvNombrePerfil)


        txtEncabezado.setText("Reporte Chats")

        val txt_i_fechaIni :  TextInputLayout = dialogReportes.findViewById(R.id.txt_i_Fecha_Ini)
        txt_i_fechaIni.editText?.setText(getFechaActual())
        txt_i_fechaIni.setEndIconOnClickListener {
            obtenerFecha(txt_i_fechaIni)
        }

        spnChats = dialogReportes.findViewById(R.id.spnChats)

        ckFecha = dialogReportes.findViewById(R.id.ckFecha)
        ckCliente = dialogReportes.findViewById(R.id.ckCliente)
        cktodosChats = dialogReportes.findViewById(R.id.cktodosChats)
        spnChats = dialogReportes.findViewById(R.id.spnChats)
        txtFechaIni = dialogReportes.findViewById<EditText>(R.id.txtFechaIni)


        ckFecha.setOnCheckedChangeListener { _, isChecked ->

            spnChats.isEnabled = !isChecked
            ckCliente.isEnabled = !isChecked
            cktodosChats.isEnabled = !isChecked
        }

        ckCliente.setOnCheckedChangeListener { _, isChecked ->

            txtFechaIni.isEnabled = !isChecked
            ckFecha.isEnabled = !isChecked
            cktodosChats.isEnabled = !isChecked
        }

        cktodosChats.setOnCheckedChangeListener { _, isChecked ->
            txtFechaIni.isEnabled = !isChecked
            spnChats.isEnabled = !isChecked
            ckCliente.isEnabled = !isChecked
            ckFecha.isEnabled = !isChecked
        }

        val retrofitGet = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiChats = retrofitGet.create(ChatsService::class.java)

        obtenerDatosDelWebServiceChats()

        val btnEnviar : Button = dialogReportes.findViewById(R.id.btnGenerarR)

        btnEnviar.setOnClickListener {
            generatePDFReportChat()
        }
        dialogReportes.show()
    }
    private fun obtenerDatosDelWebServiceChats() {
        val call = apiChats.getChats()
        call.enqueue(object : Callback<List<Chats>> {
            override fun onResponse(
                call: Call<List<Chats>>,
                response: Response<List<Chats>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val datosfiltrados = datos.filter { it.id_trabajador == idTrabajador }
                        val nombres = datosfiltrados.map { it.cliente }
                        val ids = datosfiltrados.map { it.id_chat }
                        val adapter = ArrayAdapter(
                            this@Reportes,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnChats.adapter = adapter
                        spnChats.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idChat = ids[position]
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                    }
                } else {
                }
            }
            override fun onFailure(call: Call<List<Chats>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }
    // REPORTE CHATS

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generatePDFReportChat() {
        GlobalScope.launch(Dispatchers.Main) {
            val jsonArrayChat = fetchDataFromServerChat()
            val jsonArrayMensaje = fetchDataFromServerMensaje()
            if (jsonArrayChat != null && jsonArrayMensaje!=null) {
                val pdfUri = createPDFChat(jsonArrayChat,jsonArrayMensaje)
                if (pdfUri != null) {
                    openPDF(pdfUri.path!!)
                }
            }
        }
    }

    private suspend fun fetchDataFromServerChat(): JSONArray? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://pruebarender-t81z.onrender.com/home/Chat/")
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) JSONArray(responseData) else null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    private fun createPDFChat(jsonArrayChat: JSONArray,jsonArrayMensaje: JSONArray): Uri? {
        val fileName = "ReportChats_${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
        }.pdf"

        val document = Document(PageSize.A4.rotate())

        val carpetadocumentos =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val filePath = File(carpetadocumentos, fileName)


        val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))
        class CustomPageEvent : PdfPageEventHelper() {
            override fun onEndPage(writer: PdfWriter, document: Document) {
                val pageNumber = writer.pageNumber
                val pageSize = document.pageSize

                val font = Font(Font.FontFamily.HELVETICA, 10f)
                val phrase = Phrase("Página $pageNumber", font)

                val canvas = writer.directContent
                val xPos = (pageSize.width + document.leftMargin() - document.rightMargin()) / 2
                val yPos = document.bottomMargin() - 10

                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, xPos, yPos, 0f)

                val d1 = getDrawable(R.drawable.pdf_chat)
                val bitmap2 = (d1 as BitmapDrawable).bitmap
                val stream2 = ByteArrayOutputStream()
                bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream2)
                val bitmapData2 = stream2.toByteArray()
                val image2 = Image.getInstance(bitmapData2)

                //image2.scaleToFit(200f, 100f)
                image2.scaleToFit(document.pageSize.width, 80f)

                image2.setAbsolutePosition(0f, document.pageSize.height - image2.scaledHeight)

                document.add(image2)


                val Negrita = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
                val title = Paragraph("REPORTE CHATS", Negrita)
                title.alignment = Element.ALIGN_RIGHT
                val fechayhora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val dateParagraph = Paragraph("Fecha del Reporte: $fechayhora", Negrita)
                dateParagraph.alignment = Element.ALIGN_LEFT
                val cb = writer.directContent
                val xPosTitle = (pageSize.width + document.leftMargin() - document.rightMargin()) / 2
                val xPosDate = pageSize.width - document.rightMargin()


                if (pageNumber > 1) {
                    val yPosHeader = pageSize.top - document.topMargin() - 0
                    val yPosDate = image2.absoluteY - 30f
                    val xPosDate = document.leftMargin()
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, Phrase(dateParagraph), xPosDate, yPosDate, 0f)
                } else {
                    val yPosHeader = pageSize.top - document.topMargin() - 0
                    val yPosDate = image2.absoluteY - 30f
                    val xPosDate = document.leftMargin()
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, Phrase(dateParagraph), xPosDate, yPosDate, 0f)
                }
            }
        }
        document.open()
        val customPageEvent = CustomPageEvent()
        writer.pageEvent = customPageEvent

        val Negrita = Font(
            Font.FontFamily.HELVETICA,
            12f,
            Font.BOLD
        )

        val table: PdfPTable
        val columnWidths: FloatArray

        var clienteAnterior: String? = null

        if(ckCliente.isChecked)
        {
            table = PdfPTable(4)
            columnWidths = floatArrayOf( 3f, 2f, 4f, 5f)
            table.setWidths(columnWidths)
            table.addCell(PdfPCell(Phrase("Trabajador", Negrita)))
            table.addCell(PdfPCell(Paragraph("Fecha", Negrita)))
            table.addCell(PdfPCell(Paragraph("Hora", Negrita)))
            table.addCell(PdfPCell(Paragraph("Mensaje", Negrita)))
        }
        else
        {
            table = PdfPTable(5)
            columnWidths = floatArrayOf( 3f,3f, 2f, 4f, 5f)
            table.setWidths(columnWidths)
            table.addCell(PdfPCell(Paragraph("Cliente", Negrita)))
            table.addCell(PdfPCell(Paragraph("Trabajador", Negrita)))
            table.addCell(PdfPCell(Paragraph("Fecha", Negrita)))
            table.addCell(PdfPCell(Paragraph("Hora", Negrita)))
            table.addCell(PdfPCell(Paragraph("Mensaje", Negrita)))
        }
        val espacio = Paragraph("     ")
        val espacio2 = Paragraph ("     ")
        for (i in 0 until jsonArrayChat.length()) {
            val jsonObject = jsonArrayChat.getJSONObject(i)
            val id_chat = jsonObject.getInt("id_chat")
            val id_traba = jsonObject.getInt("id_trabajador")
            val cliente = jsonObject.getString("cliente")
            val trabajador = jsonObject.getString("trabajador")
            val fec = txtFechaIni.text.toString()
            if (cktodosChats.isChecked && idTrabajador == id_traba) {
                for (e in 0 until jsonArrayMensaje.length()) {
                    val jsonObjectMensaje = jsonArrayMensaje.getJSONObject(e)
                    val mensaje = jsonObjectMensaje.getString("Mensaje")
                    val fecha = jsonObjectMensaje.getString("fecha_envio")
                    val zonedDateTime = ZonedDateTime.parse(fecha)
                    val formatohora = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val formatofecha =
                        zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    table.addCell(cliente)
                    table.addCell(trabajador)
                    table.addCell(formatofecha)
                    table.addCell(formatohora)
                    table.addCell(mensaje)
                }
            } else if (id_chat == idChat) {
                for (e in 0 until jsonArrayMensaje.length()) {
                    val jsonObjectMensaje = jsonArrayMensaje.getJSONObject(e)
                    val id_chatM = jsonObjectMensaje.getInt("id_chat")
                    val mensaje = jsonObjectMensaje.getString("Mensaje")
                    val fecha = jsonObjectMensaje.getString("fecha_envio")
                    val zonedDateTime = ZonedDateTime.parse(fecha)
                    val formatohora = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    val formatofecha =
                        zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    if (id_chatM == idChat) {
                        if (ckFecha.isChecked && formatofecha == fec) {
                            table.addCell(cliente)
                            table.addCell(trabajador)
                            table.addCell(formatofecha)
                            table.addCell(formatohora)
                            table.addCell(mensaje)
                        } else if (ckCliente.isChecked) {
                            document.add(espacio2)
                            table.addCell(trabajador)
                            table.addCell(formatofecha)
                            table.addCell(formatohora)
                            table.addCell(mensaje)
                        }
                    }

                }
            }

        }

        document.add(espacio)
        document.add(espacio)
        document.add(table)
        document.close()
        return Uri.fromFile(filePath)
    }

    private suspend fun fetchDataFromServerMensaje(): JSONArray? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://pruebarender-t81z.onrender.com/home/Mensaje/")
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) JSONArray(responseData) else null
        }
    }

    // DIALOGGGG CITAAAA
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialogReportesCita() {
        val dialogReportes = Dialog(this)
        dialogReportes.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogReportes.setCancelable(true)
        dialogReportes.setContentView(R.layout.dialog_reportecita)
        dialogReportes.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtEncabezado : TextView = dialogReportes.findViewById(R.id.txtvNombrePerfil)

        val spinner: Spinner = dialogReportes.findViewById(R.id.spnEstadosCitas)
        val datosEstaticos = arrayOf("Finalizado", "En proceso", "Aceptada")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, datosEstaticos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOption = datosEstaticos[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejar la falta de selección aquí si es necesario
            }
        })

        txtEncabezado.setText("Reporte Citas")

        val txt_i_fechaIni :  TextInputLayout = dialogReportes.findViewById(R.id.txt_i_Fecha_IniCita)
        txt_i_fechaIni.editText?.setText(getFechaActual())
        txt_i_fechaIni.setEndIconOnClickListener {
            obtenerFecha(txt_i_fechaIni)
        }

        spnClientesCita = dialogReportes.findViewById(R.id.spnClientesCita)

        spnEstadoCita=dialogReportes.findViewById(R.id.spnEstadosCitas)

        ckCliente = dialogReportes.findViewById(R.id.ckClienteCita)

        ckFecha = dialogReportes.findViewById(R.id.ckFechaCita)

        ckestadoCita = dialogReportes.findViewById(R.id.ckEstadoCita)

        cktodosChats = dialogReportes.findViewById(R.id.cktodosCitas)

        txtFechaIni = dialogReportes.findViewById(R.id.txtFechaIniCita)



        ckCliente.setOnCheckedChangeListener { _, isChecked ->
            // Deshabilitar el DatePicker y otros CheckBox
            txtFechaIni.isEnabled = !isChecked
            ckFecha.isEnabled = !isChecked
            spnEstadoCita.isEnabled=!isChecked
            ckestadoCita.isEnabled = !isChecked
            cktodosChats.isEnabled = !isChecked

        }

        ckFecha.setOnCheckedChangeListener { _, isChecked ->
            // Deshabilitar el Spinner y otros CheckBox
            spnClientesCita.isEnabled = !isChecked
            ckCliente.isEnabled = !isChecked
            cktodosChats.isEnabled = !isChecked
            spnEstadoCita.isEnabled=!isChecked
            ckestadoCita.isEnabled = !isChecked
        }

        ckestadoCita.setOnCheckedChangeListener { _, isChecked ->
            // Deshabilitar el Spinner y otros CheckBox
            spnClientesCita.isEnabled = !isChecked
            ckCliente.isEnabled = !isChecked

            txtFechaIni.isEnabled = !isChecked
            ckFecha.isEnabled = !isChecked

            cktodosChats.isEnabled = !isChecked

        }

        cktodosChats.setOnCheckedChangeListener { _, isChecked ->
            // Deshabilitar la fecha, el Spinner y el otro CheckBox
            spnClientesCita.isEnabled = !isChecked
            ckCliente.isEnabled = !isChecked

            txtFechaIni.isEnabled = !isChecked
            ckFecha.isEnabled = !isChecked

            spnEstadoCita.isEnabled = !isChecked
            ckestadoCita.isEnabled = !isChecked

        }

        val retrofitGet = Retrofit.Builder()
            .baseUrl(urlUbicMedic) // Reemplaza con la URL de tu web service
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiCita = retrofitGet.create(CitasService::class.java)

        obtenerDatosDelWebServiceCitas()

        val btnEnviar : Button = dialogReportes.findViewById(R.id.btnGenerarR)

        btnEnviar.setOnClickListener {
            generatePDFReportCita()
        }
        dialogReportes.show()
    }
    private fun obtenerDatosDelWebServiceCitas() {
        val call = apiCita.getCita()
        call.enqueue(object : Callback<List<Citas>> {
            override fun onResponse(
                call: Call<List<Citas>>,
                response: Response<List<Citas>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val datosfiltrados = datos.filter { it.id_trabajador == idTrabajador }.distinctBy { it.id_cliente }
                        val cliente = datosfiltrados.map { it.cliente }
                        val Estado = datosfiltrados.map { it.estadoid }
                        val idCita = datosfiltrados.map { it.id_cita }

                        val adaptercliente = ArrayAdapter(
                            this@Reportes,
                            android.R.layout.simple_spinner_item,
                            cliente
                        )
                        adaptercliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnClientesCita.adapter = adaptercliente
                        spnClientesCita.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idCitasg = idCita[position]
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // No se seleccionó ningún elemento
                            }
                        }

                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<Citas>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }

    //REPORTE CITA
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generatePDFReportCita() {
        GlobalScope.launch(Dispatchers.Main) {
            val jsonArray = fetchDataFromServerCita()
            if (jsonArray != null) {
                val pdfUri = createPDFCita(jsonArray)
                if (pdfUri != null) {
                    openPDF(pdfUri.path!!)
                }
            }
        }
    }
    private suspend fun fetchDataFromServerCita(): JSONArray? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://pruebarender-t81z.onrender.com/home/Cita/")
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) JSONArray(responseData) else null
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    var clientecita : String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPDFCita(jsonArray: JSONArray): Uri? {
        val fileName = "ReportCita_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
//        val file = File(getExternalFilesDir(null), fileName)
        val document = Document(PageSize.A4.rotate())

        val carpetadocumentos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val filePath = File(carpetadocumentos, fileName)


        val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))



        class CustomPageEvent : PdfPageEventHelper() {
            override fun onEndPage(writer: PdfWriter, document: Document) {
                val pageNumber = writer.pageNumber
                val pageSize = document.pageSize

                val font = Font(Font.FontFamily.HELVETICA, 10f)
                val phrase = Phrase("Página $pageNumber", font)

                val canvas = writer.directContent
                val xPos = (pageSize.width + document.leftMargin() - document.rightMargin()) / 2
                val yPos = document.bottomMargin() - 10

                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, xPos, yPos, 0f)

                val Negrita = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
                val title = Paragraph("UBICMEDIC - LISTADO DE CITAS", Negrita)
                title.alignment = Element.ALIGN_CENTER
                val fechayhora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val dateParagraph = Paragraph("Fecha del Reporte: $fechayhora", Negrita)
                dateParagraph.alignment = Element.ALIGN_RIGHT
                val cb = writer.directContent
                val xPosTitle = (pageSize.width + document.leftMargin() - document.rightMargin()) / 2
                val xPosDate = pageSize.width - document.rightMargin()

                if (pageNumber > 1) {
                    val yPosHeader = pageSize.top - document.topMargin() -0
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, Phrase(title), xPosTitle, yPosHeader, 0f)
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, Phrase(dateParagraph), xPosDate, yPosHeader, 0f)
                } else {
                    val yPosHeader = pageSize.top - 50
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, Phrase(title), xPosTitle, yPosHeader, 0f)
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, Phrase(dateParagraph), xPosDate, yPosHeader, 0f)
                }
            }
        }

        document.open()
        val customPageEvent = CustomPageEvent()
        writer.pageEvent = customPageEvent

        val Negrita = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)

        val table : PdfPTable
        val columnWidths: FloatArray

        if(ckCliente.isChecked)
        {
            table = PdfPTable(7)
            //table.widthPercentage = 100f
            columnWidths = floatArrayOf(2f, 4f, 2f, 2f,2f,2f,2f)
            table.setWidths(columnWidths)


            table.addCell(PdfPCell(Paragraph("Trabajador",Negrita)))
            //table.addCell(PdfPCell(Paragraph("Cliente",Negrita)))
            table.addCell(PdfPCell(Paragraph("Motivo",Negrita)))
            table.addCell(PdfPCell(Paragraph("Fecha Creada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Hora Creada",Negrita)))

            table.addCell(PdfPCell(Paragraph("Fecha Finalizada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Hora Finalizada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Estado",Negrita)))
        }
        else
        {
            table = PdfPTable(8)
            columnWidths = floatArrayOf(2f, 2f, 4f, 2f, 2f,2f,2f,2f)

            table.setWidths(columnWidths)
            table.addCell(PdfPCell(Paragraph("Trabajador",Negrita)))
            table.addCell(PdfPCell(Paragraph("Cliente",Negrita)))
            table.addCell(PdfPCell(Paragraph("Motivo",Negrita)))
            table.addCell(PdfPCell(Paragraph("Fecha Creada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Hora Creada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Fecha Finalizada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Hora Finalizada",Negrita)))
            table.addCell(PdfPCell(Paragraph("Estado",Negrita)))
        }

        var clienteAnterior: String? = null
        val espacio2 = Paragraph ("     ")
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val trabajador = jsonObject.getString("trabajador")
            val idtraba=jsonObject.getString("id_trabajador")
            clientecita = jsonObject.getString("id_cliente")
            val cliente = jsonObject.getString("cliente")
            val motivo = jsonObject.getString("descripcion_motivo")
            val fcreacion = jsonObject.getString("fecha_creacion")
            val zonedDateTime = ZonedDateTime.parse(fcreacion)
            val formatohora = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            val formatofecha = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val ffinal = jsonObject.getString("fecha_finatencion")
            val zonedDateTimef = ZonedDateTime.parse(ffinal)
            val formatohoraf = zonedDateTimef.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            val formatofechaf = zonedDateTimef.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val estado = jsonObject.getString("estadoid")

            val itemseleccionadoCliente = spnClientesCita.selectedItem
            val itemseleccionadoEstado = spnEstadoCita.selectedItem
            if (itemseleccionadoCliente != null) {
                val textoseleccionadoCliente = itemseleccionadoCliente.toString() // Convierte el objeto a texto
                val textoseleccionadoEstado = itemseleccionadoEstado.toString() // Convierte el objeto a texto
                val fecre = txtFechaIni.text.toString();


                if(ckFecha.isChecked && formatofecha==fecre && idtraba==idTrabajador.toString())
                {
                    table.addCell(trabajador)
                    table.addCell(cliente)
                    table.addCell(motivo)

                    table.addCell(formatofecha)
                    table.addCell(formatohora)

                    table.addCell(formatofechaf)
                    table.addCell(formatohoraf)

                    table.addCell(estado)
                }
                else if(ckCliente.isChecked && cliente==textoseleccionadoCliente && idtraba==idTrabajador.toString())
                {
                    if (cliente != clienteAnterior) {
                        val clienteParagraph = Paragraph("Cliente: $cliente", Negrita)
                        clienteParagraph.alignment = Element.ALIGN_LEFT
                        document.add(espacio2)
                        document.add(clienteParagraph)
                        clienteAnterior = cliente
                    }
                    table.addCell(trabajador)
                    //table.addCell(cliente)
                    table.addCell(motivo)

                    table.addCell(formatofecha)
                    table.addCell(formatohora)

                    table.addCell(formatofechaf)
                    table.addCell(formatohoraf)

                    table.addCell(estado)
                }
                else if(ckestadoCita.isChecked && estado==selectedOption && idtraba==idTrabajador.toString())
                {
                    table.addCell(trabajador)
                    table.addCell(cliente)
                    table.addCell(motivo)

                    table.addCell(formatofecha)
                    table.addCell(formatohora)

                    table.addCell(formatofechaf)
                    table.addCell(formatohoraf)

                    table.addCell(estado)
                }
                else if(cktodosChats.isChecked && !ckFecha.isChecked && !ckCliente.isChecked && !ckestadoCita.isChecked)
                {
                    if (idtraba==idTrabajador.toString())
                    {
                        table.addCell(trabajador)
                        table.addCell(cliente)
                        table.addCell(motivo)

                        table.addCell(formatofecha)
                        table.addCell(formatohora)

                        table.addCell(formatofechaf)
                        table.addCell(formatohoraf)

                        table.addCell(estado)
                    }

                }

            }


        }
        val espacio = Paragraph ("     ")
        document.add(espacio)
        document.add(espacio)
        document.add(table)
        document.close()

        return Uri.fromFile(filePath)
    }

    // REPORTE CLIENTE
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generatePDFReportCli() {
        GlobalScope.launch(Dispatchers.Main) {

            val jsonArray  = fetchDataFromServerCli()
            val jsonArrayCita = fetchDataFromServerCita()
            if (jsonArray != null&&jsonArrayCita !=null) {
                val pdfUri = createPDFCli(jsonArrayCita,jsonArray)
                if (pdfUri != null) {
                    openPDF(pdfUri.path!!)
                }
            }
        }
    }
    private suspend fun fetchDataFromServerCli(): JSONArray? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://pruebarender-t81z.onrender.com/home/Cliente/")
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) JSONArray(responseData) else null
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")

    suspend fun loadImageFromUrl(url: String): Image? {
        return withContext(Dispatchers.IO) {
            try {
                val imageUrl = URL(url)
                val connection = imageUrl.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val byteArray = inputStream.readBytes()
                val image = Image.getInstance(byteArray)
                inputStream.close()
                image
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createPDFCli(jsonArrayCita: JSONArray, jsonArrayCli: JSONArray): Uri? {
        val fileName = "ReportCliente_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"

        val document = Document(PageSize.A4.rotate())

        val carpetadocumentos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val filePath = File(carpetadocumentos, fileName)

        val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))

        class CustomPageEvent : PdfPageEventHelper() {
            override fun onEndPage(writer: PdfWriter, document: Document) {
                val pageNumber = writer.pageNumber
                val pageSize = document.pageSize

                val font = Font(Font.FontFamily.HELVETICA, 10f)
                val phrase = Phrase("Página $pageNumber", font)

                val canvas = writer.directContent
                val xPos = (pageSize.width + document.leftMargin() - document.rightMargin()) / 2
                val yPos = document.bottomMargin() - 10

                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase, xPos, yPos, 0f)

                val Negrita = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
                val title = Paragraph("UBICMEDIC - LISTADO DE CLIENTES", Negrita)
                title.alignment = Element.ALIGN_CENTER
                val fechayhora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val dateParagraph = Paragraph("Fecha del Reporte: $fechayhora", Negrita)
                dateParagraph.alignment = Element.ALIGN_RIGHT
                val cb = writer.directContent
                val xPosTitle = (pageSize.width + document.leftMargin() - document.rightMargin()) / 2
                val xPosDate = pageSize.width - document.rightMargin()

                if (pageNumber > 1) {
                    val yPosHeader = pageSize.top - document.topMargin() -0
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, Phrase(title), xPosTitle, yPosHeader, 0f)
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, Phrase(dateParagraph), xPosDate, yPosHeader, 0f)
                } else {
                    val yPosHeader = pageSize.top - 50
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, Phrase(title), xPosTitle, yPosHeader, 0f)
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, Phrase(dateParagraph), xPosDate, yPosHeader, 0f)
                }
            }
        }

        document.open()
        val customPageEvent = CustomPageEvent()
        writer.pageEvent = customPageEvent

        val Negrita = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)

        val table = PdfPTable(6)

        val columnWidths = floatArrayOf(1f,2f, 1.5f, 3f, 1f, 1.5f)
        table.setWidths(columnWidths)

        table.addCell(PdfPCell(Paragraph("ID Cliente",Negrita)))
        table.addCell(PdfPCell(Paragraph("Foto",Negrita)))
        table.addCell(PdfPCell(Paragraph("Cédula",Negrita)))
        table.addCell(PdfPCell(Paragraph("Nombre",Negrita)))
        table.addCell(PdfPCell(Paragraph("Tipo de Sangre",Negrita)))
        table.addCell(PdfPCell(Paragraph("Teléfono",Negrita)))

        for (i in 0 until jsonArrayCli.length()) {
            val jsonObjectCli = jsonArrayCli.getJSONObject(i)
            val fotocl = jsonObjectCli.getString("foto")
            val idClienteCli = jsonObjectCli.getInt("id_cliente")
            val cedula = jsonObjectCli.getString("cedula")
            val nombre = jsonObjectCli.getString("nombre")
            val apellido = jsonObjectCli.getString("apellido")
            val tipoSangre = jsonObjectCli.getString("sangredescrip")
            val telefono = jsonObjectCli.getString("telefono")
            var noRepetir = false
            for (e in 0 until jsonArrayCita.length()) {
                val jsonObjectCita = jsonArrayCita.getJSONObject(e)
                val idClienteCita = jsonObjectCita.getInt("id_cliente")
                val Cliente = "$nombre $apellido"
                if (idClienteCita.toString() == idClienteCli.toString() && !noRepetir) {
                    val image = loadImageFromUrl(fotocl)
                    table.addCell(idClienteCita.toString())
                    if (image != null) {
                        image.scaleToFit(100f,100f)
                        val imageCell = PdfPCell(image )
                        imageCell.setPadding(5f)
                        imageCell.borderWidth = 1f
                        table.addCell(imageCell)
                    } else {
                    }
                    table.addCell(cedula)
                    table.addCell(Cliente)
                    table.addCell(tipoSangre)
                    table.addCell(telefono)
                    noRepetir = true
                }
            }

        }
        val espacio = Paragraph ("     ")
        document.add(espacio)
        document.add(espacio)
        document.add(table)
        document.close()
        return Uri.fromFile(filePath)
    }
    private fun openPDF(filePath: String) {
        val pdfFile = File(filePath)
        val pdfUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFechaActual(): String {
        val fechaActual = LocalDate.now()
        val fechaFormato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return fechaActual.format(fechaFormato)
    }
    private fun obtenerFecha(txtfecha : TextInputLayout) {
        val calendario = Calendar.getInstance()
        val año = calendario[Calendar.YEAR]
        val mes = calendario[Calendar.MONTH]
        val día = calendario[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { view, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d-%02d-%02d", year, (month + 1), dayOfMonth)
                txtfecha.editText?.setText(selectedDate)
            }, año, mes, día
        )
        datePickerDialog.show()
    }
}

