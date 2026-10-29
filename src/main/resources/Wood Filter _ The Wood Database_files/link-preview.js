jQuery(document).ready(function($) {
    let previewCache = {};
    let hoverTimer;
    let previewBox = $('<div id="link-preview-box"></div>').appendTo('body');

    $(document).on('mouseenter', 'a.hw, a.sw, a.mo', function(e) {
        let link = $(this);
        let postUrl = link.attr('href');

        hoverTimer = setTimeout(function() {
            previewBox.html('<div class="loading-spinner"></div>').show();
            positionPreviewBox(e);

            if (previewCache[postUrl]) {
                displayPreview(previewCache[postUrl], postUrl);
                return;
            }
            
            $.ajax({
                url: linkPreview.ajax_url,
                type: 'POST',
                data: {
                    action: 'get_link_preview_data',
                    security: linkPreview.nonce,
                    post_url: postUrl
                },
                success: function(response) {
                    if (response.success) {
                        previewCache[postUrl] = response.data;
                        displayPreview(response.data, postUrl);
                    } else {
                        previewBox.hide();
                    }
                },
                error: function() {
                    previewBox.hide();
                }
            });
        }, 300);
    });

    $(document).on('mouseleave', 'a.hw, a.sw, a.mo', function() {
        clearTimeout(hoverTimer);
        previewBox.hide();
    });
    
    function positionPreviewBox(e) {
        let top = e.pageY + 20;
        let left = e.pageX + 20;
        let boxWidth = previewBox.outerWidth();
        let boxHeight = previewBox.outerHeight();
        let screenWidth = $(window).width();
        let screenHeight = $(window).height();

        if (left + boxWidth > screenWidth) {
            left = e.pageX - boxWidth - 20;
        }
        if (top + boxHeight > screenHeight) {
            top = e.pageY - boxHeight - 20;
        }
        previewBox.css({ top: top, left: left });
    }

    function displayPreview(data, url) {
        let content = `<a href="${url}" class="preview-wrapper-link">`;
        
        content += '<div class="preview-wrapper">';

        if (data.image_url) {
            content += `<div class="preview-image"><img src="${data.image_url}" alt="" /></div>`;
        }

        content += '<div class="preview-text">';
        
        // MODIFIED: Title is now a <p> tag, not an <h4>.
        if (data.title) {
            content += `<p class="preview-title">${data.title}</p>`;
        }
        if (data.scientific_name) {
            content += `<p class="scientific-name"><em>${data.scientific_name}</em></p>`;
        }
        if (data.distribution) {
            content += `<p class="distribution">${data.distribution}</p>`;
        }
        if (data.weight_lbs && data.weight_metric) {
            content += `<p class="weight">${data.weight_lbs} lbs/ft<sup>3</sup> <span class="metric">${data.weight_metric} kg/m<sup>3</sup></span></p>`;
        }
        
        // MODIFIED: The "Read more…" line has been removed.
        
        content += '</div>';
        content += '</div>';
        content += '</a>';

        previewBox.html(content);
    }
});