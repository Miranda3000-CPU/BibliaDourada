package com.example.bibliadourada.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ripple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliadourada.data.model.SearchResult
import com.example.bibliadourada.data.model.SearchResultType
import com.example.bibliadourada.theme.GoldAmber
import com.example.bibliadourada.theme.GoldBadge
import com.example.bibliadourada.theme.GoldBorder
import com.example.bibliadourada.theme.GoldChampagne
import com.example.bibliadourada.theme.GoldDeepBackground
import com.example.bibliadourada.theme.GoldImperial
import com.example.bibliadourada.theme.GoldRadiant
import com.example.bibliadourada.theme.GoldSurface
import com.example.bibliadourada.theme.GoldSurfaceVariant
import com.example.bibliadourada.theme.GoldTextPrimary
import com.example.bibliadourada.theme.GoldTextSecondary
import com.example.bibliadourada.ui.SearchUiState
import com.example.bibliadourada.ui.components.parseHtmlToAnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  searchState: SearchUiState,
  onQueryChange: (String) -> Unit,
  onFilterChange: (SearchResultType) -> Unit,
  onResultClick: (SearchResult) -> Unit,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(GoldDeepBackground)
      .padding(horizontal = 16.dp)
  ) {
    // Cabeçalho
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "Pesquisar nas Escrituras",
      color = GoldImperial,
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Serif
    )
    Text(
      text = "Busque por termos nos versículos e estudos bíblicos",
      color = GoldTextSecondary,
      fontSize = 13.sp,
      fontFamily = FontFamily.Serif
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Campo de Busca Estilizado em Dourado
    OutlinedTextField(
      value = searchState.query,
      onValueChange = onQueryChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = {
        Text("Ex: esperança, amor, luz, fé...", color = GoldTextSecondary.copy(alpha = 0.6f))
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = GoldImperial
        )
      },
      trailingIcon = {
        if (searchState.query.isNotEmpty()) {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Limpar busca",
              tint = GoldAmber
            )
          }
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = GoldSurface,
        unfocusedContainerColor = GoldSurface,
        focusedBorderColor = GoldImperial,
        unfocusedBorderColor = GoldBorder,
        focusedTextColor = GoldTextPrimary,
        unfocusedTextColor = GoldTextPrimary,
        cursorColor = GoldImperial
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filtros em Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = searchState.filter == SearchResultType.ALL,
        onClick = { onFilterChange(SearchResultType.ALL) },
        label = { Text("Todos") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = GoldImperial,
          selectedLabelColor = Color(0xFF1F1600),
          containerColor = GoldSurface,
          labelColor = GoldTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
          borderColor = GoldBorder,
          selectedBorderColor = GoldImperial,
          enabled = true,
          selected = searchState.filter == SearchResultType.ALL
        )
      )

      FilterChip(
        selected = searchState.filter == SearchResultType.VERSE,
        onClick = { onFilterChange(SearchResultType.VERSE) },
        label = { Text("Versículos") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = GoldImperial,
          selectedLabelColor = Color(0xFF1F1600),
          containerColor = GoldSurface,
          labelColor = GoldTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
          borderColor = GoldBorder,
          selectedBorderColor = GoldImperial,
          enabled = true,
          selected = searchState.filter == SearchResultType.VERSE
        )
      )

      FilterChip(
        selected = searchState.filter == SearchResultType.COMMENT,
        onClick = { onFilterChange(SearchResultType.COMMENT) },
        label = { Text("Estudos") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = GoldImperial,
          selectedLabelColor = Color(0xFF1F1600),
          containerColor = GoldSurface,
          labelColor = GoldTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
          borderColor = GoldBorder,
          selectedBorderColor = GoldImperial,
          enabled = true,
          selected = searchState.filter == SearchResultType.COMMENT
        )
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Resultados
    if (searchState.isSearching) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = GoldImperial)
      }
    } else if (searchState.query.trim().length >= 2 && searchState.results.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = GoldAmber.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Nenhum resultado encontrado para \"${searchState.query}\"",
            color = GoldTextSecondary,
            fontSize = 15.sp,
            fontFamily = FontFamily.Serif
          )
        }
      }
    } else if (searchState.query.trim().length < 2) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = GoldAmber.copy(alpha = 0.3f),
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Digite ao menos 2 letras para pesquisar",
            color = GoldTextSecondary.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif
          )
        }
      }
    } else {
      Text(
        text = "${searchState.results.size} resultados encontrados",
        color = GoldAmber,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 6.dp)
      )

      LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        // O índice entra na chave: um banco importado pode trazer ids repetidos
        // (ex.: coluna `rowid` vazia no índice FTS) e uma chave duplicada
        // derrubaria a tela inteira.
        itemsIndexed(
          searchState.results,
          key = { index, result -> "${result.type}_${result.id}_$index" }
        ) { _, result ->
          SearchResultCard(
            result = result,
            onClick = { onResultClick(result) }
          )
        }
      }
    }
  }
}

@Composable
private fun SearchResultCard(
  result: SearchResult,
  onClick: () -> Unit
) {
  val isVerse = result.type == SearchResultType.VERSE
  val parsedText = remember(result.text) {
    parseHtmlToAnnotatedString(result.text)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(GoldSurface)
      .border(1.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = GoldImperial.copy(alpha = 0.25f)),
        onClick = onClick
      )
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Referência (Ex: Salmos 23:1)
        Text(
          text = if (result.verseNumber > 0) {
            "${result.bookName} ${result.chapterNumber}:${result.verseNumber}"
          } else {
            "${result.bookName} ${result.chapterNumber}"
          },
          color = GoldImperial,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Serif
        )

        // Badge indicador se é Versículo ou Comentário
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isVerse) GoldSurfaceVariant else GoldAmber.copy(alpha = 0.2f))
            .border(
              0.8.dp,
              if (isVerse) GoldBorder else GoldAmber,
              RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = if (isVerse) "Versículo" else "✦ Estudo",
            color = if (isVerse) GoldChampagne else GoldBadge,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Trecho do texto
      Text(
        text = parsedText,
        color = GoldTextPrimary,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        fontFamily = if (isVerse) FontFamily.Serif else FontFamily.Default
      )
    }
  }
}
