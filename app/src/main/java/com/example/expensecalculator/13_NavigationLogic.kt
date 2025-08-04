import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expensecalculator.ExpenseScreen
import com.example.expensecalculator.MainScreen
import com.example.expensecalculator.TripManager.AddTripScreen
import com.example.expensecalculator.TripManager.EditTripScreen
import com.example.expensecalculator.TripManager.FirstScreen
import com.example.expensecalculator.TripManager.TripMainScreen
import com.example.expensecalculator.TripManager.TripViewModel
import com.example.expensecalculator.viewModel

@Composable
fun NavGraph(navController: NavHostController,viewModel: viewModel,tripViewModel: TripViewModel){

    NavHost(navController = navController, startDestination = "first_screen"){


        composable("first_screen"){
            FirstScreen(navController = navController)
        }

        composable("detail_screen"){
            MainScreen(navController = navController, viewModel = viewModel)
        }

        composable("trip_details"){
            TripMainScreen(navController = navController, viewModel = tripViewModel)
        }

        composable("add_trip"){
            AddTripScreen(navController = navController, viewModel = tripViewModel)
        }
        composable("edit_trip/{tripId}", arguments = listOf(navArgument("tripId"){
            type= NavType.IntType
        })){
            backStackEntry->
            val id=backStackEntry.arguments?.getInt("tripId")
            if(id!=null){
                EditTripScreen(navController = navController, viewModel = tripViewModel, tripId = id)
            }

        }

        composable(route="expense_screen/{detailId}", arguments = listOf(navArgument("detailId"){ type=
            NavType.IntType }))

        {backStackEntry->
            val id=backStackEntry.arguments?.getInt("detailId")

            if(id!=null){
                ExpenseScreen(
                    viewModel=viewModel,
                    accountId = id,
                    onNavigateBack = {navController.popBackStack()}
                )
            }


        }
    }
}