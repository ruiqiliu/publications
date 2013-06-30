
-- Invoke as osascript OmniGraffleConverter.scpt file.graffle file.pdf
-- http://eventless.net/scripts/automatic-export-from-omnigraffle-to-pdf

on run argv
	if length of argv is less than 2 then
		return "Usage: OmniGraffleConverter.scpt  "
	end if
	
	set infile to item 1 of argv
	set outfile to item 2 of argv
	
	set infile to (POSIX file infile) as string
	set outfile to (POSIX file outfile) as string
	
	tell application "OmniGraffle Professional 5"
		activate
		
		open file infile
		save front window in outfile
		quit application saving no
	end tell
end run
