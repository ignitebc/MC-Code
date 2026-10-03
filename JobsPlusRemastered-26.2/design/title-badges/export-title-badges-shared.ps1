param(
    [string]$SourcePath = (Join-Path $PSScriptRoot 'title-badges-shared.png')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$textureDirectory = Join-Path $PSScriptRoot '../../common/src/main/resources/assets/jobsplus/textures'
$guiDirectory = Join-Path $textureDirectory 'gui/title_badge'
$fontDirectory = Join-Path $textureDirectory 'font/title_badge'

# 사용자가 선택한 원본의 픽셀과 투명도를 그대로 복사한다.
# 1152 x 256 공통 캔버스에서만 축소본을 만들어 표시 위치마다 디자인이 달라지지 않게 한다.
$sprites = @(
    @{ Id = 'nether_star'; X = 102; Y = 154; Width = 1051; Height = 232 },
    @{ Id = 'seal_breaker'; X = 97; Y = 420; Width = 1061; Height = 218 },
    @{ Id = 'last_strike'; X = 67; Y = 658; Width = 1120; Height = 235 },
    @{ Id = 'ten_thousand_souls'; X = 95; Y = 893; Width = 1065; Height = 254 }
)

$source = [Drawing.Bitmap]::FromFile((Resolve-Path -LiteralPath $SourcePath).Path)
try {
    if ($source.Width -ne 1254 -or $source.Height -ne 1254 -or $source.GetPixel(0, 0).A -ne 0) {
        throw 'The selected source must be the 1254 x 1254 transparent title badge sheet.'
    }
    [IO.Directory]::CreateDirectory($guiDirectory) | Out-Null
    [IO.Directory]::CreateDirectory($fontDirectory) | Out-Null
    foreach ($sprite in $sprites) {
        $gui = [Drawing.Bitmap]::new(1152, 256, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $font = [Drawing.Bitmap]::new(252, 56, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [Drawing.Graphics]::FromImage($font)
        $attributes = [Drawing.Imaging.ImageAttributes]::new()
        try {
            $left = [int][Math]::Floor(($gui.Width - $sprite.Width) / 2)
            $top = [int][Math]::Floor(($gui.Height - $sprite.Height) / 2)
            for ($y = 0; $y -lt $sprite.Height; $y++) {
                for ($x = 0; $x -lt $sprite.Width; $x++) {
                    $gui.SetPixel($left + $x, $top + $y, $source.GetPixel($sprite.X + $x, $sprite.Y + $y))
                }
            }
            $gui.Save((Join-Path $guiDirectory ($sprite.Id + '.png')), [Drawing.Imaging.ImageFormat]::Png)

            # 바닐라 글꼴의 글리프는 256 x 256 아틀라스에 들어가야 한다.
            # GUI와 종횡비·여백을 같게 두고 252 x 56으로 한 번만 축소한다.
            $graphics.Clear([Drawing.Color]::Transparent)
            $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
            $graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
            $attributes.SetWrapMode([Drawing.Drawing2D.WrapMode]::TileFlipXY)
            $graphics.DrawImage($gui, [Drawing.Rectangle]::new(0, 0, 252, 56),
                    0, 0, 1152, 256, [Drawing.GraphicsUnit]::Pixel, $attributes)
            $font.Save((Join-Path $fontDirectory ($sprite.Id + '.png')), [Drawing.Imaging.ImageFormat]::Png)
            Write-Output ($sprite.Id + ': original pixels -> GUI 1152 x 256 -> font 252 x 56')
        }
        finally {
            $attributes.Dispose()
            $graphics.Dispose()
            $font.Dispose()
            $gui.Dispose()
        }
    }
}
finally { $source.Dispose() }
