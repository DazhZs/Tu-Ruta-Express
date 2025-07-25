package com.turutaexpress.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.turutaexpress.navigation.AppNavigation

@Composable
fun MainScreen() {
    Scaffold { paddingValues ->
        AppNavigation(modifier = Modifier.padding(paddingValues))
    }
}