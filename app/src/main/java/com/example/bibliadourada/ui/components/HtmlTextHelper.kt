package com.example.bibliadourada.ui.components

import android.graphics.Typeface
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat

fun parseHtmlToAnnotatedString(html: String): AnnotatedString {
  if (html.isEmpty()) return AnnotatedString("")
  
  val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
  val rawString = spanned.toString()
  val builder = AnnotatedString.Builder(rawString)

  val styleSpans = spanned.getSpans(0, spanned.length, StyleSpan::class.java)
  for (span in styleSpans) {
    val start = spanned.getSpanStart(span).coerceIn(0, rawString.length)
    val end = spanned.getSpanEnd(span).coerceIn(start, rawString.length)
    when (span.style) {
      Typeface.BOLD -> builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
      Typeface.ITALIC -> builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
      Typeface.BOLD_ITALIC -> builder.addStyle(
        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
        start,
        end
      )
    }
  }

  val underlineSpans = spanned.getSpans(0, spanned.length, UnderlineSpan::class.java)
  for (span in underlineSpans) {
    val start = spanned.getSpanStart(span).coerceIn(0, rawString.length)
    val end = spanned.getSpanEnd(span).coerceIn(start, rawString.length)
    builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
  }

  return builder.toAnnotatedString()
}
