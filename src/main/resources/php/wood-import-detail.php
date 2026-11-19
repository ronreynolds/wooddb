<?php
require 'wood-db.php';  // defines mysqli connection

$id = (int)($_GET['id'] ?? 0);
if ($id <= 0) {
    http_response_code(400);
    exit('Invalid id');
}

$stmt = $mysqli->prepare("SELECT * FROM wood_yaml_import WHERE id = ?");
$stmt->bind_param("i", $id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    // Group fields for display
    $sections = [
        'Identity' => [
            'binomial'           => ['label' => 'Binomial', 'italic' => true],
            'commonName'         => ['label' => 'Common Name'],
            'commonNamesTsv'     => ['label' => 'Other Common Names'],
            'distribution'       => ['label' => 'Distribution'],
            'treeSize'           => ['label' => 'Tree Size'],
            'pageUrl'            => ['label' => 'Source Page', 'link' => true],
        ],
        'Appearance & Properties' => [
            'colorAndAppearance'   => ['label' => 'Color & Appearance'],
            'grainAndTexture'      => ['label' => 'Grain & Texture'],
            'odor'                 => ['label' => 'Odor'],
            'rotResistance'        => ['label' => 'Rot Resistance'],
            'workability'          => ['label' => 'Workability'],
            'allergiesAndToxicity' => ['label' => 'Allergies & Toxicity'],
            'pricingAndAvailability' => ['label' => 'Pricing & Availability'],
            'sustainability'       => ['label' => 'Sustainability'],
            'commonUses'           => ['label' => 'Common Uses'],
            'comments'             => ['label' => 'Comments'],
        ],
        'Physical Data' => [
            'averageDryWeight' => ['label' => 'Avg Dry Weight (lb/ft³)'],
            'specificGravity'  => ['label' => 'Specific Gravity'],
            'jankaHardness'    => ['label' => 'Janka Hardness (lbf)'],
            'ruptureModulus'   => ['label' => 'Rupture Modulus (psi)'],
            'elasticModulus'   => ['label' => 'Elastic Modulus (psi)'],
            'crushingStrength' => ['label' => 'Crushing Strength (psi)'],
            'shrinkage'        => ['label' => 'Shrinkage'],
        ],
        'Anatomy' => [
            'porosity'     => ['label' => 'Porosity'],
            'arrangement'  => ['label' => 'Arrangement'],
            'vessels'      => ['label' => 'Vessels'],
            'parenchyma'   => ['label' => 'Parenchyma'],
            'rays'         => ['label' => 'Rays'],
        ],
        'Other' => [
            'lookalikes' => ['label' => 'Lookalikes'],
            'notes'      => ['label' => 'Notes'],
        ],
    ];

    $imageFields = [
        'imageUrl'         => 'General',
        'endGrainImage'    => 'End Grain',
        'endGrain10xImage' => 'End Grain 10×',
        'edgeGrainImage'   => 'Edge Grain',
    ];
} else {
    http_response_code(404);
    exit('Record not found');
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title><?= htmlspecialchars($row['binomial'] ?? 'Wood Detail') ?></title>
    <link rel="stylesheet" href="style.css" />
</head>
<body>
<h1><span class="binomial"><?= htmlspecialchars($row['binomial'] ?? '') ?></span></h1>
<?php if (!empty($row['commonName'])) { ?>
    <p style="color:#666; margin-top:-0.5rem;"><?= htmlspecialchars($row['commonName']) ?></p>
<?php } ?>

<!-- Images -->
<?php if (array_filter(array_map(fn($k) => $row[$k] ?? '', array_keys($imageFields)))): ?>
<div class="images">
    <?php foreach ($imageFields as $col => $label) { ?>
        <?php if (!empty($row[$col])) { ?>
        <figure>
            <img src="<?= htmlspecialchars($row[$col]) ?>" alt="<?= $label ?>"/>
            <figcaption><?= $label ?></figcaption>
        </figure>
        <?php } // end if
        ?>
    <?php } // end foreach image-fields
    ?>
</div>
<?php endif; ?>

<!-- Field sections -->
<?php
    foreach ($sections as $heading => $fields) {
        $hasContent = false;
        foreach ($fields as $col => $opts) {
            if (!empty($row[$col])) { $hasContent = true; break; }
        }
        if ($hasContent) {
    ?>
    <h2><?= htmlspecialchars($heading) ?></h2>
    <table class="fields">
        <?php
            foreach ($fields as $col => $opts) {
                $val = $row[$col] ?? '';
                if (empty($val)) continue;
            ?>
            <tr>
                <td><?= htmlspecialchars($opts['label']) ?></td>
                <td>
                    <?php if (isset($opts['link'])): ?>
                        <a href="<?= htmlspecialchars($val) ?>" target="_blank"><?= htmlspecialchars($val) ?></a>
                    <?php elseif (isset($opts['italic'])): ?>
                        <em><?= htmlspecialchars($val) ?></em>
                    <?php else: ?>
                        <?= nl2br(htmlspecialchars($val)) ?>
                    <?php endif; ?>
                </td>
            </tr>
        <?php
            } // end foreach fields
        ?>
    </table>
    <?php
        } // end if hasContent
    } // end foreach sections
    ?>
    <?php include 'jquery-sort-th.js'?>

<p class="meta">
    Imported <?= htmlspecialchars(date('M j, Y H:i', strtotime($row['importDate']))) ?>
    &middot; ID: <?= $row['id'] ?>
    <?php if ($row['processed']): ?> &middot; Processed<?php endif; ?>
</p>

</body>
</html>
<?php $mysqli->close(); ?>