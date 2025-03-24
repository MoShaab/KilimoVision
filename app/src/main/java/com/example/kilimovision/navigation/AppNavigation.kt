package com.example.kilimovision.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kilimovision.ui.screens.*
import com.example.kilimovision.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val userType by authViewModel.userType.collectAsState()

    // For debugging
    Log.d("AppNavigation", "Starting with auth state: $authState, user type: $userType")

    // Start at the landing page
    NavHost(
        navController = navController,
        startDestination = "landing"
    ) {
        // Landing page - Role selection
        composable("landing") {
            Log.d("AppNavigation", "Composing landing screen")

            LandingScreen(
                onContinueAsFarmer = {
                    Log.d("AppNavigation", "LandingScreen: Continue as farmer clicked")
                    navController.navigate("login/farmer")
                },
                onContinueAsSeller = {
                    Log.d("AppNavigation", "LandingScreen: Continue as seller clicked")
                    navController.navigate("login/seller")
                }
            )
        }

        // Authentication flows with role parameter
        composable(
            "login/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "farmer"

            LoginScreen(
                role = role,
                onNavigateToRegister = {
                    navController.navigate("register/$role")
                },
                onLoginSuccess = { userType ->
                    Log.d("AppNavigation", "Login success with user type: $userType")
                    when (userType) {
                        "farmer" -> navController.navigate("farmer_main") {
                            popUpTo("login/$role") { inclusive = true }
                        }
                        "seller" -> navController.navigate("seller_dashboard") {
                            popUpTo("login/$role") { inclusive = true }
                        }
                        else -> {
                            // Default to farmer main screen if unknown type
                            Log.d("AppNavigation", "Unknown user type, defaulting to farmer")
                            navController.navigate("farmer_main") {
                                popUpTo("login/$role") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(
            "register/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "farmer"

            RegisterScreen(
                role = role,
                onNavigateToLogin = {
                    navController.navigate("login/$role") {
                        popUpTo("register/$role") { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    when (role) {
                        "farmer" -> navController.navigate("farmer_main")
                        "seller" -> {
                            // For sellers, go to profile creation first
                            val userId = authViewModel.getCurrentUserId() ?: ""
                            navController.navigate("seller_registration/$userId")
                        }
                    }
                    // Clear the back stack
                    navController.popBackStack("register/$role", inclusive = true)
                }
            )
        }

        composable(
            "seller_registration/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val userId = authViewModel.getCurrentUserId() ?: ""
            SellerRegistrationScreen(
                userId = userId,
                onRegistrationComplete = {
                    navController.navigate("seller_dashboard") {
                        popUpTo("seller_registration/$userId") { inclusive = true }
                    }
                },
                onCancelRegistration = {
                    authViewModel.signOut() // Sign out if registration is cancelled
                    navController.navigate("landing") {
                        popUpTo("seller_registration/$userId") { inclusive = true }
                    }
                }
            )
        }

        // Farmer main screen
        composable("farmer_main") {
            FarmerMainScreen(
                onNavigateToSellers = { disease ->
                    navController.navigate("agro_sellers/$disease")
                },
                onNavigateToProfile = {
                    navController.navigate("farmer_profile")
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate("landing") {
                        popUpTo("farmer_main") { inclusive = true }
                    }
                },
                onRedirectToLanding = {
                    navController.navigate("landing") {
                        popUpTo("farmer_main") { inclusive = true }
                    }
                }
            )
        }

        // Farmer profile
        composable("farmer_profile") {
            FarmerProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "agro_sellers/{disease}",
            arguments = listOf(navArgument("disease") { type = NavType.StringType })
        ) { backStackEntry ->
            val disease = backStackEntry.arguments?.getString("disease") ?: ""
            AgroSellerScreen( // Changed from AgroSellerFilterScreen to match previous fixes
                detectedDisease = disease,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        // Seller app flows
        composable("seller_dashboard") {
            SellerDashboardScreen(
                navController = navController,
                onNavigateToProfile = {
                    navController.navigate("seller_profile")
                },
                onNavigateToRegister = {
                    navController.navigate("seller_registration/{userId}")
                },

                onLogout = {
                    authViewModel.signOut()
                    navController.navigate("landing") {
                        popUpTo("seller_dashboard") { inclusive = true }
                    }

                },
                onRedirectToLanding = {
                    navController.navigate("landing") {
                        popUpTo("seller_dashboard") { inclusive = true }
                    }
                }



            )
        }

        // Seller profile
        composable("seller_profile") {
            SellerProfileScreen(

                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { sellerId ->
                    navController.navigate("add_product/$sellerId") },
                onNavigateToCreateAd = { sellerId ->
                    navController.navigate("create_advertisement/$sellerId")
                }
            )
        }

        composable(
            "add_product/{sellerId}",
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType })

        ) {backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            AddProductScreen(
                sellerId = sellerId,
                onBackPressed = {
                    navController.popBackStack()
                },
                onProductCreated = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            "create_advertisement/{sellerId}",
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            CreateAdvertisementScreen(
                sellerId = sellerId,
                onBackPressed = {
                    navController.popBackStack()
                },
                onAdCreated = {
                    navController.popBackStack()
                }
            )
        }
    }
}