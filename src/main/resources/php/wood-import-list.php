<?php
require 'wood-db.php';  // defines mysqli connection
//require 'common-functions.php';

$sql = "SELECT * FROM wood_yaml_import ORDER BY latinBinomial";
$result = $mysqli->query($sql);
?>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Wood Import List</title>
        <link rel="stylesheet" href="style.css" />
    </head>
<body>
    <h1>Wood Import Records</h1>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Binomial(s)</th>
                <th>Common Name (CSV)</th>
                <th>SG</th>
                <th>Janka</th>
                <th>Porosity</th>
                <th>Arrangement</th>
                <th>Vessels</th>
                <th>Parenchyma</th>
                <th>Rays</th>
            </tr>
        </thead>
        <tbody>
        <?php
        while ($row = $result->fetch_assoc()) {
            $id = (int)$row['id'];
            $binomial = htmlspecialchars($row['binomial']);
            $latinBinomial = htmlspecialchars($row['latinBinomial']);
            $commonName = htmlspecialchars($row['commonName'] ?? '');
            $commonNamesCsv = htmlspecialchars($row['commonNamesTsv'] ?? '');

            $specificGravity = htmlspecialchars($row['specificGravity'] ?? '');
            $jankaHardness = htmlspecialchars($row['jankaHardness'] ?? '');
            $porosity = htmlspecialchars($row['porosity'] ?? '');
            $arrangement = htmlspecialchars($row['arrangement'] ?? '');
            $vessels = htmlspecialchars($row['vessels'] ?? '');
            $parenchyma = htmlspecialchars($row['parenchyma'] ?? '');
            $rays = htmlspecialchars($row['rays'] ?? '');
        ?>
            <tr>
                <td><a href="wood-import-detail.php?id=<?=$id?>" target="_blank"><?=$id?></a></td>
                <td><span class="binomial"><?=$binomial?> (<?=$latinBinomial?>)</span></a></td>
                <td><?=$commonName?>(<?=$commonNamesCsv?>)</td>
                <td><?=$specificGravity?></td>
                <td><?=$jankaHardness?></td>
                <td><?=$porosity?></td>
                <td><?=$arrangement?></td>
                <td><?=$vessels?></td>
                <td><?=$parenchyma?></td>
                <td><?=$rays?></td>
            </tr>
        <?php } // end while
        ?>
        </tbody>
    </table>
    <?php include 'jquery-sort-th.js'?>
    </body>
</html>
<?php $mysqli->close(); ?>