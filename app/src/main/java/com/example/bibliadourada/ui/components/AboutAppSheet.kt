package com.example.bibliadourada.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.bibliadourada.R
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldChampagne
import com.example.bibliadourada.theme.GoldDeepBackground
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldRadiant
import com.example.bibliadourada.theme.GoldSurface
import com.example.bibliadourada.theme.GoldTextPrimary
import com.example.bibliadourada.theme.GoldTextSecondary

private const val DEVELOPER_NAME = "Jeiel Miranda"
private const val DEVELOPER_URL = "https://jeielmiranda.com.br"
private const val NOTES_URL =
  "https://www.universal.org/noticias/post/chegou-a-versao-digital-da-biblia-com-as-anotacoes-do-bispo-edir-macedo/"
private const val CONTACT_EMAIL = "JeielMirand@gmail.com"
private const val POETRY_APP_URL = "https://is.gd/MeuVerso"

@Suppress("DEPRECATION")
private fun appVersionName(context: Context): String =
  runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
  }.getOrNull().orEmpty().ifBlank { "—" }

private fun openUrl(context: Context, url: String) {
  runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
}

private fun openEmail(context: Context, address: String) {
  val intent = Intent(
    Intent.ACTION_SENDTO,
    "mailto:$address".toUri()
  ).putExtra(Intent.EXTRA_SUBJECT, "Contato - Bíblia Dourada")
  runCatching { context.startActivity(intent) }
}

/**
 * Modal "Sobre o aplicativo": versão, autoria, origem das anotações,
 * contato e divulgação do outro app do mesmo criador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppSheet(
  onDismiss: () -> Unit,
  onOpenImport: () -> Unit = {},
  onReplayTour: () -> Unit = {},
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val context = LocalContext.current
  val versionName = remember { appVersionName(context) }

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
      // ---------------------------------------------------------------- topo
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(R.drawable.ic_logo_biblia),
          contentDescription = null,
          modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = "Bíblia Dourada",
            color = GoldImperial,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
          )
          Text(
            text = "Edição de Estudo & Meditação",
            color = GoldTextSecondary,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(GoldSurface)
              .border(1.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Versão $versionName",
              color = GoldAmber,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
      HorizontalDivider(color = GoldBorder.copy(alpha = 0.5f), thickness = 1.dp)
      Spacer(modifier = Modifier.height(20.dp))

      // ------------------------------------------------------------- sobre
      SectionTitle("Sobre o aplicativo")
      BodyText(
        "O aplicativo foi criado com o intuito de oferecer estudo bíblico de forma gratuita, " +
          "sem coleta de dados, sem anúncios e sem qualquer cobrança."
      )

      Spacer(modifier = Modifier.height(12.dp))

      LinkRow(
        label = "Importar dados",
        caption = "Carregar um banco (.db) ou anotações (.json) do seu aparelho",
        icon = { Icon(Icons.Default.Storage, null, tint = GoldAmber, modifier = Modifier.size(16.dp)) },
        onClick = onOpenImport
      )

      Spacer(modifier = Modifier.height(10.dp))

      LinkRow(
        label = "Ver o tour novamente",
        caption = "Relembrar como navegar pelo aplicativo",
        icon = { Icon(Icons.Default.HelpOutline, null, tint = GoldAmber, modifier = Modifier.size(16.dp)) },
        onClick = onReplayTour
      )

      Spacer(modifier = Modifier.height(20.dp))

      // ------------------------------------------------------ desenvolvido
      SectionTitle("Desenvolvido por")
      LinkRow(
        label = DEVELOPER_NAME,
        caption = DEVELOPER_URL.removePrefix("https://"),
        icon = { Icon(Icons.Default.OpenInNew, null, tint = GoldAmber, modifier = Modifier.size(16.dp)) },
        onClick = { openUrl(context, DEVELOPER_URL) }
      )

      Spacer(modifier = Modifier.height(20.dp))

      // ---------------------------------------------------------- anotações
      SectionTitle("Anotações bíblicas")
      BodyText(
        "As anotações são do Bispo Edir Macedo, bispo atual da Igreja Universal do Reino de " +
          "Deus — CNPJ 29.744.778/0001-97."
      )
      Spacer(modifier = Modifier.height(10.dp))
      LinkRow(
        label = "Chegou a versão digital da Bíblia com as anotações do Bispo Edir Macedo",
        caption = "universal.org",
        icon = { Icon(Icons.Default.OpenInNew, null, tint = GoldAmber, modifier = Modifier.size(16.dp)) },
        onClick = { openUrl(context, NOTES_URL) }
      )
      Spacer(modifier = Modifier.height(12.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(GoldSurface)
          .border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
          .padding(14.dp)
      ) {
        Text(
          text = "O aplicativo é distribuído SEM essas anotações, para respeitar os direitos " +
            "autorais de quem as produziu. O conteúdo é importado pelo próprio usuário, a " +
            "partir de um arquivo que ele já possua, sem fins lucrativos e sem qualquer " +
            "afiliação. Este projeto nasceu do desejo pessoal de construir um aplicativo " +
            "próprio de estudo.",
          color = GoldTextSecondary,
          fontSize = 13.sp,
          lineHeight = 19.sp
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // ------------------------------------------------------------ contato
      SectionTitle("Contato")
      BodyText("Dúvidas, sugestões ou correções sobre o app:")
      Spacer(modifier = Modifier.height(10.dp))
      LinkRow(
        label = CONTACT_EMAIL,
        caption = "Abrir o app de e-mail",
        icon = { Icon(Icons.Default.Email, null, tint = GoldAmber, modifier = Modifier.size(16.dp)) },
        onClick = { openEmail(context, CONTACT_EMAIL) }
      )

      Spacer(modifier = Modifier.height(24.dp))

      // --------------------------------------------------- outro app / autor
      SectionTitle("Outro app do mesmo criador")

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(GoldSurface)
          .border(1.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
          .padding(16.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Meu Verso",
              color = GoldChampagne,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Serif,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "is.gd/MeuVerso",
              color = GoldAmber,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "MeuVerso é o seu refúgio poético. Um espaço dedicado à escrita, leitura e " +
              "compartilhamento de versos que tocam a alma. Explore temas variados, siga seus " +
              "autores favoritos e publique suas próprias criações em uma interface elegante e " +
              "minimalista. Transforme sentimentos em palavras com o MeuVerso.",
            color = GoldTextPrimary,
            fontSize = 14.sp,
            lineHeight = 21.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "✨ Funcionalidades",
            color = GoldRadiant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          FeatureRow("✍️", "Criação de Versos", "Ferramenta simples e elegante para escrever seus poemas.")
          FeatureRow("📖", "Leitura Imersiva", "Design focado na legibilidade para uma experiência de leitura tranquila.")
          FeatureRow("👥", "Comunidade", "Siga seus autores favoritos e descubra novos talentos.")
          FeatureRow("🎨", "Interface Minimalista", "Foco total no conteúdo, sem distrações.")
          FeatureRow("📱", "Mobile First", "Desenvolvido nativamente para Android.")

          Spacer(modifier = Modifier.height(16.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(GoldImperial)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAmber.copy(alpha = 0.3f)),
                onClick = { openUrl(context, POETRY_APP_URL) }
              )
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Conhecer o Meu Verso",
              color = GoldDeepBackground,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Feito com carinho para leitura e meditação da Palavra. ✦",
        color = GoldTextSecondary,
        fontSize = 12.sp,
        fontFamily = FontFamily.Serif
      )
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    color = GoldRadiant,
    fontSize = 15.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = FontFamily.Serif
  )
  Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun BodyText(text: String) {
  Text(
    text = text,
    color = GoldTextPrimary,
    fontSize = 14.sp,
    lineHeight = 21.sp
  )
}

@Composable
private fun LinkRow(
  label: String,
  caption: String,
  icon: @Composable () -> Unit,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(GoldSurface)
      .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = GoldImperial.copy(alpha = 0.25f)),
        onClick = onClick
      )
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        color = GoldImperial,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = caption,
        color = GoldTextSecondary,
        fontSize = 11.sp
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    icon()
  }
}

@Composable
private fun FeatureRow(emoji: String, title: String, description: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(text = emoji, fontSize = 14.sp)
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = GoldTextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = description,
        color = GoldTextSecondary,
        fontSize = 12.sp,
        lineHeight = 17.sp
      )
    }
  }
}
