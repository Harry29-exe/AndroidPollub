package pl.kwojcik.za.app

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "players")
class Player(
    val name: String,
    val email: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Entity(tableName = "results")
class Result(
    val score: Int,
    val playerId: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

data class PlayerResult(
    val playerName: String,
    val score: Int
) {}

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(player: Player): Long
}

@Dao
interface ResultDao {
    @Insert
    suspend fun insert(score: Result): Long
}

@Dao
interface PlayerResultDao {
    @Query("SELECT r.score as score, p.name as playerName FROM results r JOIN players p on (p.id = r.playerId)")
    suspend fun getPlayerResults(): List<PlayerResult>
}

@Database(
    entities = [Player::class, Result::class],
    version = 1,
    exportSchema = false
)
abstract class MasterAndDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun resultDao(): ResultDao
    abstract fun playerResultsDao(): PlayerResultDao

    companion object {
        @Volatile
        private var Instance: MasterAndDatabase? = null
        fun getDatabase(context: Context): MasterAndDatabase {
            return Room.databaseBuilder(
                context,
                MasterAndDatabase::class.java,
                "master_and_database"
            )
                .build().also { Instance = it }
        }
    }

}