(function ($) {
  $(function () {
    if ('object' !== typeof FWP) {
      return;
    }

    FWP.hooks.addFilter('facetwp/template_html', function (resp, params) {
      if (FWP.is_load_more) {
        FWP.is_load_more = false;

        var $newHtml = $(params.html);

        // existing container from the first page render
        var $existingRow = $('.facetwp-template .row').first();

        // items from the newly-loaded page
        var $newItems = $newHtml.find('.row').first().children();

        if ($existingRow.length && $newItems.length) {
          $existingRow.append($newItems);
          return true;
        }

        // fallback if the template structure changes
        $('.facetwp-template').append(params.html);
        return true;
      }

      return resp;
    });
  });

  var throttleTimer = null,
    throttleDelay = 100,
    isLoading = false,
    footerHeight = 0; // set later

  function scrollHandler() {
    clearTimeout(throttleTimer);
    throttleTimer = setTimeout(function () {
      // measure footer after DOM exists
      footerHeight = $('footer').height() + 1000;

      if ((($(document).height() - $(window).height()) > ($(window).scrollTop() + footerHeight))) {
        return;
      }

      if (isLoading) {
        return;
      }

      // guard in case pager data isn't present for some reason
      if (!FWP.settings || !FWP.settings.pager) {
        return;
      }

      if (FWP.settings.pager.page < FWP.settings.pager.total_pages) {
        isLoading = true;
        FWP.paged = parseInt(FWP.settings.pager.page, 10) + 1;
        FWP.is_load_more = true;
        FWP.soft_refresh = false;
        FWP.refresh();
      } else {
        $('.fwp-loader').hide();
      }
    }, throttleDelay);
  }

  $(document).on('facetwp-loaded', function () {
    isLoading = false;
    $('.fwp-loader').hide();

    if (!FWP.loaded) {
      $(window).off('scroll', scrollHandler).on('scroll', scrollHandler);

      // prevent duplicate loaders
      if (0 === $('.fwp-loader').length) {
$('.facetwp-template').after(
  '<div class="fwp-loader" aria-live="polite" aria-busy="true">' +
    '<span class="wdb-spinner wdb-spinner--xl" aria-hidden="true">' +
      '<img class="wdb-spinner__img" src="https://www.wood-database.com/log-spinner.webp" alt="">' +
    '</span>' +
  '</div>'
);
      }
    }
  });

  $(document).on('facetwp-refresh', function () {
    $('.fwp-loader').show();
  });
})(jQuery);