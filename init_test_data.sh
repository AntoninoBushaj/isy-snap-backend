#!/bin/bash

# ========================================
# IsySnap - Initialize Test Data
# ========================================
# This script initializes test data in the database
# It's safe to run multiple times - won't modify existing data
# ========================================

echo "🚀 Initializing IsySnap test data..."
echo ""

# Execute SQL script in Docker container
docker exec -i isysnap_mysql mysql -uroot -proot isysnap < src/main/resources/db/init_test_data.sql 2>&1 | grep -v "Warning"

echo ""
echo "✅ Test data initialization completed!"
echo ""
echo "📝 Admin credentials:"
echo "   Email: admin@gmail.com"
echo "   Password: admin2025"
echo ""
echo "🏪 Test restaurants created:"
echo "   1. La Bella Vita (Italian)"
echo "   2. Sushi Tokyo (Japanese)"
echo "   3. Burger House (American)"
echo ""
echo "🍕 Total menu items: 25+"
echo "🪑 Total tables: 7"
echo ""