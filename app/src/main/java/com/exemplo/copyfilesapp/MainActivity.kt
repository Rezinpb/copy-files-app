package com.exemplo.copyfilesapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private var sourceUri: Uri? = null
    private val REQUEST_CODE_SOURCE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSelectSource = findViewById<Button>(R.id.btnSelectSource)
        val btnCopyToDownloads = findViewById<Button>(R.id.btnCopyToDownloads)

        btnSelectSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_CODE_SOURCE)
        }

        btnCopyToDownloads.setOnClickListener {
            sourceUri?.let { uri -> copyFiles(uri) } ?: showToast("Selecione a pasta de origem")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SOURCE && resultCode == Activity.RESULT_OK) {
            sourceUri = data?.data
            contentResolver.takePersistableUriPermission(
                sourceUri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            showToast("Pasta de origem selecionada.")
        }
    }

    private fun copyFiles(sourceUri: Uri) {
        val sourceDoc = DocumentFile.fromTreeUri(this, sourceUri)
        val downloadsUri = DocumentsContract.buildDocumentUriUsingTree(
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload"),
            "primary:Download"
        )
        val destDoc = DocumentFile.fromTreeUri(this, downloadsUri)

        sourceDoc?.listFiles()?.forEach { file ->
            val inputStream = contentResolver.openInputStream(file.uri)
            val newFile = destDoc?.createFile(file.type ?: "application/octet-stream", file.name ?: "arquivo")
            val outputStream: OutputStream? = newFile?.uri?.let { contentResolver.openOutputStream(it) }
            inputStream?.copyTo(outputStream!!, 1024)
            inputStream?.close()
            outputStream?.close()
        }
        showToast("Arquivos copiados para Downloads.")
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
