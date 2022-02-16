package cz.cleevio.cache

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

	companion object {
		private const val TEST_DB = "migration-test"

	}

	// Array of all migrations
	private val allMigrations = arrayOf<Migration>(
		/*CleevioDatabase.MIGRATION_1_2*/
	)

	@get:Rule
	val helper: MigrationTestHelper = MigrationTestHelper(
		InstrumentationRegistry.getInstrumentation(),
		CleevioDatabase::class.java.canonicalName,
		FrameworkSQLiteOpenHelperFactory()
	)

	@Test
	@Throws(IOException::class)
	fun migrateAll() {
		// Create earliest version of the database.
		helper.createDatabase(TEST_DB, 1).apply {
			close()
		}

		// Open latest version of the database. Room will validate the schema
		// once all migrations execute.
		Room.databaseBuilder(
			InstrumentationRegistry.getInstrumentation().targetContext,
			CleevioDatabase::class.java,
			TEST_DB
		).addMigrations(*allMigrations).build().apply {
			openHelper.writableDatabase
			close()
		}
	}
}