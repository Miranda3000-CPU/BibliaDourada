package com.example.bibliadourada.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.data.DatabaseSummary
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldDeepBackground
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldRadiant
import com.example.bibliadourada.theme.GoldSurface
import com.example.bibliadourada.theme.GoldSurfaceVariant
import com.example.bibliadourada.theme.GoldTextPrimary
import com.example.bibliadourada.theme.GoldTextSecondary
import com.example.bibliadourada.ui.ImportFeedback

/**
 * Ferramenta de importação de dados.
 *
 * O app é distribuído sem o banco de anotações (conteúdo de terceiros, ver
 * docs/BANCO_DE_DADOS.md). Aqui o usuário traz os dados do seu próprio
 * arquivo: um banco completo (.db) ou um JSON de anotações para mesclar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataSheet(
  summary: DatabaseSummary,
  isImporting: Boolean,
  feedback: ImportFeedback?,
  onDismiss: () -> Unit,
  onImportDatabase: (Uri) -> Unit,
  onImportAnnotations: (Uri) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  // .db não tem MIME padronizado no Android, então aceitamos qualquer arquivo.
  val databasePicker = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
  ) { uri -> uri?.let(onImportDatabase) }

  val annotationsPicker = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
  ) { uri -> uri?.let(onImportAnnotations) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = GoldDeepBackground,
    contentColor = GoldTextPrimary,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(top = 10.dp, bottom = 6.dp)
          .size(width = 44.dp, height = 4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(GoldBorder)
      )
    },
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 36.dp)
    ) {
      Text(
        text = "Dados do aplicativo",
        color = GoldImperial,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "O aplicativo é distribuído sem o conteúdo de estudo, para respeitar " +
          "os direitos autorais das anotações. Traga os seus próprios dados:",
        color = GoldTextSecondary,
        fontSize = 13.sp,
        lineHeight = 19.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      // ------------------------------------------------ conteúdo atual
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(GoldSurface)
          .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        StatCell("Livros", summary.books)
        StatCell("Versículos", summary.verses)
        StatCell("Anotações", summary.comments)
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (summary.verses == 0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldSurfaceVariant)
            .border(1.dp, GoldAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
        ) {
          Text(
            text = "O aplicativo está vazio. Importe um banco .db para carregar os " +
              "livros e versículos — só então as anotações poderão ser exibidas.",
            color = GoldTextPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // ------------------------------------------------------ ações
      ImportAction(
        title = "Importar banco de dados (.db)",
        description = "Substitui todo o conteúdo: livros, versículos e anotações.",
        enabled = !isImporting,
        onClick = { databasePicker.launch(arrayOf("*/*")) }
      )

      Spacer(modifier = Modifier.height(10.dp))

      ImportAction(
        title = "Importar anotações (.json)",
        description = if (summary.verses == 0) {
          "Disponível após importar um banco com os versículos."
        } else {
          "Acrescenta anotações ao banco atual, sem apagar o que já existe."
        },
        enabled = !isImporting && summary.verses > 0,
        onClick = { annotationsPicker.launch(arrayOf("application/json", "text/plain", "*/*")) }
      )

      // --------------------------------------------------- feedback
      if (isImporting) {
        Spacer(modifier = Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(
            color = GoldImperial,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Importando… isso pode levar alguns segundos.",
            color = GoldTextSecondary,
            fontSize = 13.sp
          )
        }
      } else if (feedback != null) {
        Spacer(modifier = Modifier.height(18.dp))
        val accent = if (feedback.isError) GoldAmber else GoldRadiant
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldSurface)
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(14.dp)
        ) {
          Text(
            text = feedback.message,
            color = GoldTextPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
      HorizontalDivider(color = GoldBorder.copy(alpha = 0.5f), thickness = 1.dp)
      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "O formato esperado dos arquivos e o esquema do banco estão " +
          "documentados em docs/BANCO_DE_DADOS.md, no repositório do projeto. " +
          "Nenhum dado sai do seu aparelho.",
        color = GoldTextSecondary,
        fontSize = 12.sp,
        lineHeight = 17.sp
      )
    }
  }
}

@Composable
private fun StatCell(label: String, value: Int) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value.toString(),
      color = GoldImperial,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = label,
      color = GoldTextSecondary,
      fontSize = 11.sp
    )
  }
}

@Composable
private fun ImportAction(
  title: String,
  description: String,
  enabled: Boolean,
  onClick: () -> Unit
) {
  val alpha = if (enabled) 1f else 0.45f
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(GoldSurface)
      .border(1.dp, GoldBorder.copy(alpha = 0.7f * alpha), RoundedCornerShape(14.dp))
      .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = GoldImperial.copy(alpha = 0.25f)),
        onClick = onClick
      )
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (title.contains(".db")) Icons.Default.Storage else Icons.Default.Description,
      contentDescription = null,
      tint = GoldAmber.copy(alpha = alpha),
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = GoldTextPrimary.copy(alpha = alpha),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        color = GoldTextSecondary.copy(alpha = alpha),
        fontSize = 12.sp,
        lineHeight = 17.sp
      )
    }
  }
}
