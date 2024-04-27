package pl.kwojcik.za.app

import android.app.Application
import android.content.Context

interface AppContainer {
    val playerRepository: PlayerRepository
    val resultRepository: ResultRepository
    val playerResultRepository: PlayerResultRepository
}

class AppDataContainer(
    private val context: Context
) : AppContainer {
    override val playerRepository: PlayerRepository by lazy {
        PlayerRepositoryImpl(MasterAndDatabase.getDatabase(context).playerDao())
    }
    override val playerResultRepository: PlayerResultRepository by lazy {
        PlayerResultRepositoryImpl(MasterAndDatabase.getDatabase(context).playerResultsDao())
    }
    override val resultRepository: ResultRepository by lazy {
        ResultRepositoryImpl(MasterAndDatabase.getDatabase(context).resultDao())
    }
}

class MasterAndApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface PlayerRepository {
    suspend fun insert(player: Player) : Long
}

class PlayerRepositoryImpl(
    private val playerDao: PlayerDao
) : PlayerRepository {
    override suspend fun insert(player: Player): Long {
        return playerDao.insert(player)
    }
}

interface ResultRepository {
    suspend fun insert(result: Result): Long
    suspend fun selectAll(): List<Result>
}

class ResultRepositoryImpl (
    private val resulDao: ResultDao
) : ResultRepository {
    override suspend fun insert(result: Result): Long {
        return resulDao.insert(result)
    }

    override suspend fun selectAll(): List<Result> {
        return resulDao.selectAll()
    }
}

interface PlayerResultRepository {
    suspend fun findRecent(): List<PlayerResult>
}

class PlayerResultRepositoryImpl(
    val playerResulDao: PlayerResultDao
) : PlayerResultRepository {
    override suspend fun findRecent(): List<PlayerResult> {
        return playerResulDao.getPlayerResults()
            .sortedBy { p -> -p.score }
    }
}


